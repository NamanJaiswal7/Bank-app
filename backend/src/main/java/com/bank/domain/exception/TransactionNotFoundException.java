package com.bank.domain.exception;

/**
 * Thrown when a transaction lookup by ID yields no result.
 *
 * <p>Used by the CQRS query side to signal that a referenced transaction
 * does not exist, without leaking persistence-layer concerns.</p>
 */
public class TransactionNotFoundException extends DomainException {

    private static final String ERROR_CODE = "TRANSACTION_NOT_FOUND";

    private final Long transactionId;

    public TransactionNotFoundException(Long transactionId) {
        super(ERROR_CODE, String.format("Transaction with ID %d was not found", transactionId));
        this.transactionId = transactionId;
    }

    public Long getTransactionId() {
        return transactionId;
    }
}
