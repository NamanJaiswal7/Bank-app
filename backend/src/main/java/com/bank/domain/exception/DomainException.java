package com.bank.domain.exception;

/**
 * Base class for all domain-specific exceptions.
 *
 * <p>Every domain exception carries a machine-readable {@code errorCode} that
 * can be mapped to HTTP status codes or API error payloads by the presentation
 * layer, keeping the domain free of transport concerns.</p>
 *
 * @author bank-app
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;

    /**
     * Constructs a new domain exception.
     *
     * @param errorCode a short, uppercase identifier for the error category
     *                  (e.g. {@code "INSUFFICIENT_FUNDS"})
     * @param message   a human-readable description of the error
     */
    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new domain exception with a root cause.
     *
     * @param errorCode a short, uppercase identifier for the error category
     * @param message   a human-readable description of the error
     * @param cause     the underlying throwable that caused this exception
     */
    protected DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the machine-readable error code associated with this exception.
     *
     * @return the error code, never {@code null}
     */
    public String getErrorCode() {
        return errorCode;
    }
}
