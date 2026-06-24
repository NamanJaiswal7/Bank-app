package com.bank.application.handler;

import com.bank.application.command.ExchangeCommand;
import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.application.port.out.ExternalValidationPort;
import com.bank.domain.event.ExchangeCompletedEvent;
import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.SameAccountExchangeException;
import com.bank.domain.factory.TransactionFactory;
import com.bank.domain.model.Account;
import com.bank.domain.model.Transaction;
import com.bank.domain.service.ExchangeRateProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles currency exchange between two accounts.
 *
 * <p>Does <em>not</em> extend {@link AbstractTransactionHandler} because the
 * exchange flow operates on two aggregates with a different shape:
 * load both → validate → convert → debit source → credit target.</p>
 *
 * <p>Validation order (cheapest first):
 * <ol>
 *   <li>Source and target must differ ({@link SameAccountExchangeException}).</li>
 *   <li>Both accounts must exist ({@link AccountNotFoundException}).</li>
 *   <li>External validation must pass on the source debit
 *       (same precondition as a plain debit, with Circuit Breaker protection).</li>
 *   <li>Source must have sufficient funds (enforced by {@link Account#debit}).</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeHandler {

    private final AccountRepositoryPort accountRepository;
    private final ExchangeRateProvider exchangeRateProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final ExternalValidationPort externalValidation;

    public List<Transaction> handle(ExchangeCommand command) {
        // 1. Fail fast on self-exchange — no DB round-trip needed.
        if (Objects.equals(command.getSourceAccountId(), command.getTargetAccountId())) {
            throw new SameAccountExchangeException(command.getSourceAccountId());
        }

        // 2. Load both accounts.
        Account source = accountRepository.findById(command.getSourceAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.getSourceAccountId()));
        Account target = accountRepository.findById(command.getTargetAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.getTargetAccountId()));

        // 3. External validation for the source debit — same precondition as plain debit.
        log.debug("Calling external validation for source account {}", source.getId());
        externalValidation.validate(source.getId());

        // 4. Convert amount.
        BigDecimal sourceAmount = command.getAmount();
        BigDecimal targetAmount = exchangeRateProvider.convert(
                sourceAmount, source.getCurrency(), target.getCurrency()
        );

        // 5. Debit source, credit target (invariants enforced by the aggregate).
        source.debit(sourceAmount);
        target.credit(targetAmount);

        // 6. Save both accounts.
        accountRepository.save(source);
        accountRepository.save(target);

        // 7. Create linked transaction pair.
        List<Transaction> transactions = TransactionFactory.createExchangePair(
                source.getId(), target.getId(),
                sourceAmount, targetAmount,
                source.getBalance(), target.getBalance(),
                source.getCurrency(), target.getCurrency()
        );

        // 8. Publish event.
        UUID referenceId = transactions.get(0).getReferenceId();
        eventPublisher.publishEvent(new ExchangeCompletedEvent(
                source.getId(), target.getId(),
                sourceAmount, targetAmount,
                source.getCurrency(), target.getCurrency(),
                referenceId
        ));

        log.debug("Exchange completed: {} {} → {} {}",
                source.getCurrency().format(sourceAmount), source.getCurrency(),
                target.getCurrency().format(targetAmount), target.getCurrency());

        return transactions;
    }
}
