package com.bank.domain.exception;

import com.bank.domain.model.Currency;

/**
 * Thrown when an operation is attempted with a currency that does not match
 * the target account's configured currency.
 *
 * <p>For example, crediting USD to a EUR account would raise this exception.</p>
 *
 * @see com.bank.domain.model.Account#validateCurrency(Currency)
 */
public class CurrencyMismatchException extends DomainException {

    private static final String ERROR_CODE = "CURRENCY_MISMATCH";

    private final Currency expected;
    private final Currency actual;

    /**
     * Constructs a new {@code CurrencyMismatchException}.
     *
     * @param expected the currency the account is denominated in
     * @param actual   the currency that was incorrectly supplied
     */
    public CurrencyMismatchException(Currency expected, Currency actual) {
        super(ERROR_CODE, String.format(
                "Currency mismatch: expected %s but received %s",
                expected.name(), actual.name()));
        this.expected = expected;
        this.actual = actual;
    }

    /**
     * @return the currency the account expected
     */
    public Currency getExpected() {
        return expected;
    }

    /**
     * @return the currency that was actually provided
     */
    public Currency getActual() {
        return actual;
    }
}
