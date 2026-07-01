package com.bank.domain.model;

import com.bank.domain.exception.InvalidCurrencyException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Supported currencies with their display symbols and decimal scales.
 */
public enum Currency {
    EUR("€", 2),
    USD("$", 2),
    SEK("kr", 2),
    GBP("£", 2),
    VND("₫", 0);

    private final String symbol;
    private final int scale;

    Currency(String symbol, int scale) {
        this.symbol = symbol;
        this.scale = scale;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getScale() {
        return scale;
    }

    /**
     * Formats a BigDecimal amount with the currency symbol.
     * Example: €100.00, ₫50000
     */
    public String format(BigDecimal amount) {
        return symbol + amount.setScale(scale, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Safely parses a client-supplied currency string.
     *
     * <p>Unlike {@link #valueOf(String)}, this method throws a
     * {@link InvalidCurrencyException} (a {@link com.bank.domain.exception.DomainException})
     * on unknown input, which the presentation layer maps to a stable HTTP 400
     * response with a descriptive message listing all supported currencies.</p>
     *
     * @param value the raw string from the client (case-insensitive, may be blank)
     * @return the matching {@code Currency}
     * @throws InvalidCurrencyException if {@code value} is null, blank, or not a supported currency
     */
    public static Currency fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidCurrencyException(String.valueOf(value), supportedList());
        }
        String normalised = value.trim().toUpperCase();
        for (Currency c : values()) {
            if (c.name().equals(normalised)) {
                return c;
            }
        }
        throw new InvalidCurrencyException(value, supportedList());
    }

    private static String supportedList() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}
