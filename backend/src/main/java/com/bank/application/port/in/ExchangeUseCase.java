package com.bank.application.port.in;

import com.bank.application.command.ExchangeCommand;
import com.bank.domain.model.Transaction;

import java.util.List;

/**
 * Inbound port for currency exchange operations.
 */
public interface ExchangeUseCase {

    /**
     * Performs a currency exchange between two accounts owned by the same user.
     *
     * @param command the exchange command
     * @return list of 2 transactions (EXCHANGE_OUT + EXCHANGE_IN)
     */
    List<Transaction> exchange(ExchangeCommand command);
}
