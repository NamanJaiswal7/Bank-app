package com.bank.domain.exception;

/**
 * Thrown when an external validation step (e.g. exchange-rate lookup,
 * compliance check) fails.
 *
 * <p>This exception acts as a domain-level wrapper around infrastructure
 * failures that the domain layer needs to communicate without leaking
 * implementation details.</p>
 */
public class ExternalValidationException extends DomainException {

    private static final String ERROR_CODE = "EXTERNAL_VALIDATION_FAILED";

    /**
     * Constructs a new {@code ExternalValidationException} without a root cause.
     *
     * @param message a human-readable description of the validation failure
     */
    public ExternalValidationException(String message) {
        super(ERROR_CODE, message);
    }

    /**
     * Constructs a new {@code ExternalValidationException} with a root cause.
     *
     * @param message a human-readable description of the validation failure
     * @param cause   the underlying exception that triggered this failure
     */
    public ExternalValidationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
