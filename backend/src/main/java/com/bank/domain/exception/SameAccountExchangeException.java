package com.bank.domain.exception;

/**
 * Thrown when an exchange request specifies the same account as both source
 * and target.
 *
 * <p>A self-exchange has no economic meaning (the conversion would credit and
 * debit the same balance), so the application rejects it at the use-case
 * boundary rather than silently no-oping.</p>
 */
public class SameAccountExchangeException extends DomainException {

    private static final String ERROR_CODE = "SAME_ACCOUNT_EXCHANGE";

    private final Long accountId;

    public SameAccountExchangeException(Long accountId) {
        super(ERROR_CODE, String.format(
                "Source and target accounts must differ (both are %d)", accountId));
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }
}
