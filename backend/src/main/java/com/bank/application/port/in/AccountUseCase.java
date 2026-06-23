package com.bank.application.port.in;

import com.bank.application.command.CreditCommand;
import com.bank.application.command.DebitCommand;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;

import java.util.List;

/**
 * Inbound port — defines what the application can do with accounts.
 */
public interface AccountUseCase {

    Account createAccount(Long userId, Currency currency);

    Account getAccount(Long accountId);

    List<Account> getAccountsByUser(Long userId);

    Transaction credit(CreditCommand command);

    Transaction debit(DebitCommand command);
}
