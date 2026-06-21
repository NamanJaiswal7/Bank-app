package com.bank.domain.exception;

/**
 * Thrown when an account lookup by ID yields no result.
 *
 * <p>Typically raised by repository or application service layers when the
 * caller references an account that does not exist.</p>
 */
public class AccountNotFoundException extends DomainException {

    private static final String ERROR_CODE = "ACCOUNT_NOT_FOUND";

    private final Long accountId;

    /**
     * Constructs a new {@code AccountNotFoundException}.
     *
     * @param accountId the ID of the account that could not be found
     */
    public AccountNotFoundException(Long accountId) {
        super(ERROR_CODE, String.format("Account with ID %d was not found", accountId));
        this.accountId = accountId;
    }

    /**
     * @return the ID of the account that was not found
     */
    public Long getAccountId() {
        return accountId;
    }
}
