package com.bank.application.handler;

import com.bank.application.command.DebitCommand;
import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.application.port.out.ExternalValidationPort;
import com.bank.domain.event.DebitExecutedEvent;
import com.bank.domain.factory.TransactionFactory;
import com.bank.domain.model.Account;
import com.bank.domain.model.Transaction;
import com.bank.domain.specification.SufficientBalanceSpecification;
import com.bank.domain.exception.InsufficientFundsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Handles debit operations.
 *
 * Overrides:
 * - preCondition() → calls external validation (httpstat.us)
 * - perform() → checks SufficientBalanceSpecification → calls account.debit()
 */
@Slf4j
@Component
public class DebitHandler extends AbstractTransactionHandler<DebitCommand> {

    private final ExternalValidationPort externalValidation;

    public DebitHandler(AccountRepositoryPort accountRepository,
                        ApplicationEventPublisher eventPublisher,
                        ExternalValidationPort externalValidation) {
        super(accountRepository, eventPublisher);
        this.externalValidation = externalValidation;
    }

    @Override
    protected Long getAccountId(DebitCommand command) {
        return command.getAccountId();
    }

    @Override
    protected void preCondition(DebitCommand command, Account account) {
        // External validation before debit (requirement: call httpstat.us)
        log.debug("Calling external validation for account {}", account.getId());
        externalValidation.validate(account.getId());
    }

    @Override
    protected Transaction perform(DebitCommand command, Account account) {
        // Specification pattern: check sufficient balance
        SufficientBalanceSpecification spec = new SufficientBalanceSpecification(command.getAmount());
        if (!spec.isSatisfiedBy(account)) {
            throw new InsufficientFundsException(
                    account.getId(), account.getBalance(),
                    command.getAmount(), account.getCurrency()
            );
        }

        account.debit(command.getAmount());
        return TransactionFactory.createDebit(
                account.getId(),
                command.getAmount(),
                account.getBalance(),
                account.getCurrency()
        );
    }

    @Override
    protected Object createEvent(DebitCommand command, Account account, Transaction transaction) {
        return new DebitExecutedEvent(
                account.getId(),
                command.getAmount(),
                account.getBalance(),
                account.getCurrency()
        );
    }
}
