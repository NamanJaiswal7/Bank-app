package com.bank.application.handler;

import com.bank.application.command.CreditCommand;
import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.domain.event.AccountCreditedEvent;
import com.bank.domain.factory.TransactionFactory;
import com.bank.domain.model.Account;
import com.bank.domain.model.Transaction;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Handles credit operations.
 * Overrides perform() — calls account.credit().
 */
@Component
public class CreditHandler extends AbstractTransactionHandler<CreditCommand> {

    public CreditHandler(AccountRepositoryPort accountRepository,
                         ApplicationEventPublisher eventPublisher) {
        super(accountRepository, eventPublisher);
    }

    @Override
    protected Long getAccountId(CreditCommand command) {
        return command.getAccountId();
    }

    @Override
    protected Transaction perform(CreditCommand command, Account account) {
        account.credit(command.getAmount());
        return TransactionFactory.createCredit(
                account.getId(),
                command.getAmount(),
                account.getBalance(),
                account.getCurrency()
        );
    }

    @Override
    protected Object createEvent(CreditCommand command, Account account, Transaction transaction) {
        return new AccountCreditedEvent(
                account.getId(),
                command.getAmount(),
                account.getBalance(),
                account.getCurrency()
        );
    }
}
