package com.bank.application.handler;

import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.domain.exception.AccountNotFoundException;
import com.bank.domain.model.Account;
import com.bank.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Template Method pattern for transaction handlers.
 *
 * Skeleton: loadAccount → preCondition → perform → save → publishEvent
 *
 * Subclasses override:
 * - preCondition() — optional (e.g., external validation for debit)
 * - perform() — the actual domain operation
 * - createEvent() — the domain event to publish
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTransactionHandler<C> {

    protected final AccountRepositoryPort accountRepository;
    protected final ApplicationEventPublisher eventPublisher;

    /**
     * Executes the full transaction flow.
     */
    public Transaction handle(C command) {
        // 1. Load account
        Account account = loadAccount(getAccountId(command));

        // 2. Pre-conditions (hook — override in subclasses)
        preCondition(command, account);

        // 3. Perform domain operation
        Transaction transaction = perform(command, account);

        // 4. Save account state
        accountRepository.save(account);

        // 5. Publish domain event
        Object event = createEvent(command, account, transaction);
        if (event != null) {
            eventPublisher.publishEvent(event);
        }

        log.debug("Handled {} for account {}", command.getClass().getSimpleName(),
                getAccountId(command));

        return transaction;
    }

    protected Account loadAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    /**
     * Hook for pre-conditions. Default is no-op.
     * Override in DebitHandler to call external validation.
     */
    protected void preCondition(C command, Account account) {
        // Default: no pre-conditions
    }

    /** Extracts the account ID from the command. */
    protected abstract Long getAccountId(C command);

    /** Performs the domain operation and returns the resulting Transaction. */
    protected abstract Transaction perform(C command, Account account);

    /** Creates the domain event to publish. Return null to skip. */
    protected abstract Object createEvent(C command, Account account, Transaction transaction);
}
