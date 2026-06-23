package com.bank.application.port.out;

import com.bank.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for Transaction persistence.
 */
public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    List<Transaction> saveAll(List<Transaction> transactions);

    Optional<Transaction> findById(Long id);

    /**
     * Find transactions for an account with pagination, ordered by timestamp descending.
     *
     * @param accountId the account ID
     * @param page      zero-based page number
     * @param size      page size
     * @return list of transactions for the given page
     */
    List<Transaction> findByAccountId(Long accountId, int page, int size);

    /**
     * Count total transactions for an account (for pagination metadata).
     */
    long countByAccountId(Long accountId);
}
