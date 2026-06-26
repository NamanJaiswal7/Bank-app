package com.bank.adapter.out.exchange;

import com.bank.domain.model.Currency;
import com.bank.domain.service.ExchangeRateProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

/**
 * Strategy implementation — fixed exchange rates (EUR-based).
 * Swap this for an API-based provider by implementing ExchangeRateProvider.
 */
@Component
public class FixedExchangeRateProvider implements ExchangeRateProvider {

    /** Rates relative to EUR (1 EUR = X currency). */
    private static final Map<Currency, BigDecimal> RATES_TO_EUR = new EnumMap<>(Currency.class);

    static {
        RATES_TO_EUR.put(Currency.EUR, BigDecimal.ONE);
        RATES_TO_EUR.put(Currency.USD, new BigDecimal("1.08"));
        RATES_TO_EUR.put(Currency.SEK, new BigDecimal("11.50"));
        RATES_TO_EUR.put(Currency.GBP, new BigDecimal("0.86"));
        RATES_TO_EUR.put(Currency.VND, new BigDecimal("27000"));
    }

    @Override
    public BigDecimal getRate(Currency from, Currency to) {
        if (from == to) {
            return BigDecimal.ONE;
        }
        // Convert: from → EUR → to
        BigDecimal fromRate = RATES_TO_EUR.get(from);
        BigDecimal toRate = RATES_TO_EUR.get(to);
        return toRate.divide(fromRate, 10, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        BigDecimal rate = getRate(from, to);
        return amount.multiply(rate).setScale(to.getScale(), RoundingMode.HALF_UP);
    }
}
