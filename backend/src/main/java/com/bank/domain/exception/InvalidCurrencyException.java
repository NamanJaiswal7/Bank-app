package com.bank.domain.exception;

/**
 * Thrown when the client supplies a currency code that is not one of the
 * supported {@link com.bank.domain.model.Currency} values.
 *
 * <p>Mapped to HTTP 400 by {@code GlobalExceptionHandler}. The message
 * always lists the supported currencies so the client can self-correct
 * without needing a separate discovery endpoint.</p>
 */
public class InvalidCurrencyException extends DomainException {

    private static final String ERROR_CODE = "INVALID_CURRENCY";

    private final String provided;

    public InvalidCurrencyException(String provided, String supported) {
        super(ERROR_CODE, String.format(
                "Currency '%s' is not supported. Supported currencies: %s",
                provided, supported));
        this.provided = provided;
    }

    public String getProvided() {
        return provided;
    }
}
