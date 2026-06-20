package com.bank.domain.model;

import java.math.BigDecimal;

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
}
