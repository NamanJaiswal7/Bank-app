package com.bank.domain.exception;

/**
 * Thrown when a user lookup by ID yields no result.
 *
 * <p>Mapped to HTTP 404 by {@code GlobalExceptionHandler}.</p>
 */
public class UserNotFoundException extends DomainException {

    private static final String ERROR_CODE = "USER_NOT_FOUND";

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super(ERROR_CODE, String.format("User with ID %d was not found", userId));
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
