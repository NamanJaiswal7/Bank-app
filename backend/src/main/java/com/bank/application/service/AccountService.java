package com.bank.application.service;

import com.bank.application.command.CreditCommand;
import com.bank.application.command.DebitCommand;
import com.bank.application.handler.CreditHandler;
import com.bank.application.handler.DebitHandler;
import com.bank.application.port.in.AccountUseCase;
import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.application.port.out.TransactionRepositoryPort;
import com.bank.application.port.out.UserRepositoryPort;
import com.bank.domain.event.AccountCreatedEvent;
import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.exception.UserNotFoundException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade — implements AccountUseCase by delegating to handlers.
 * Manages transactions and coordinates the overall flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements AccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final UserRepositoryPort userRepository;
    private final CreditHandler creditHandler;
    private final DebitHandler debitHandler;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Account createAccount(Long userId, Currency currency) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Account saved = accountRepository.save(Account.open(userId, currency));

        eventPublisher.publishEvent(
                new AccountCreatedEvent(saved.getId(), userId, currency));

        log.info("Created account {} for user {} with currency {}",
                saved.getId(), userId, currency);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    @Override
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2.0))
    public Transaction credit(CreditCommand command) {
        Transaction transaction = creditHandler.handle(command);
        return transactionRepository.save(transaction);
    }

    @Override
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2.0))
    public Transaction debit(DebitCommand command) {
        Transaction transaction = debitHandler.handle(command);
        return transactionRepository.save(transaction);
    }
}
