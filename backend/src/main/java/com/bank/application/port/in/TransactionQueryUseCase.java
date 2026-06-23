package com.bank.application.port.in;

import com.bank.domain.model.Transaction;

import java.util.List;

/**
 * Inbound port for querying transaction history (CQRS read side).
 */
public interface TransactionQueryUseCase {

    Transaction getTransaction(Long transactionId);

    /**
     * Returns paginated transaction history for an account.
     *
     * @param accountId account ID
     * @param page      zero-based page number
     * @param size      page size
     * @return list of transactions ordered by timestamp descending
     */
    List<Transaction> getTransactionsByAccount(Long accountId, int page, int size);

    /**
     * Returns total number of transactions for pagination metadata.
     */
    long countTransactionsByAccount(Long accountId);
}
