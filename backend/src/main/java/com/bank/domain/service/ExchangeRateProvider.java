package com.bank.domain.service;

import com.bank.domain.model.Currency;

import java.math.BigDecimal;

/**
 * Strategy interface for exchange rate provisioning.
 *
 * Implementations can provide hardcoded rates, fetch from an API,
 * or read from cache — the domain doesn't care how.
 */
public interface ExchangeRateProvider {

    /**
     * Returns the exchange rate from one currency to another.
     *
     * @param from source currency
     * @param to   target currency
     * @return the exchange rate (multiply source amount by this to get target amount)
     */
    BigDecimal getRate(Currency from, Currency to);

    /**
     * Converts an amount from one currency to another.
     *
     * @param amount the amount to convert
     * @param from   source currency
     * @param to     target currency
     * @return the converted amount
     */
    BigDecimal convert(BigDecimal amount, Currency from, Currency to);
}
