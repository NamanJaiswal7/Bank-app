package com.bank.adapter.out.exchange;

import com.bank.domain.model.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class FixedExchangeRateProviderTest {

    private final FixedExchangeRateProvider provider = new FixedExchangeRateProvider();

    @Test
    void sameCurrency_isIdentityRate() {
        for (Currency c : Currency.values()) {
            assertEquals(BigDecimal.ONE, provider.getRate(c, c),
                    "same-currency rate must be 1 for " + c);
            assertEquals(
                    new BigDecimal("100.00").setScale(c.getScale(), RoundingMode.HALF_UP),
                    provider.convert(new BigDecimal("100.00"), c, c));
        }
    }

    @Test
    void crossRate_isInverseOfReverseRate() {
        BigDecimal eurToUsd = provider.getRate(Currency.EUR, Currency.USD);
        BigDecimal usdToEur = provider.getRate(Currency.USD, Currency.EUR);

        BigDecimal product = eurToUsd.multiply(usdToEur)
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("1.00"), product,
                "EUR→USD × USD→EUR should round-trip to 1");
    }

    @Test
    void convert_eurToUsd_appliesRateAndScalesToTargetCurrency() {
        BigDecimal converted = provider.convert(
                new BigDecimal("100.00"), Currency.EUR, Currency.USD);
        // 100 EUR @ 1.08 = 108.00 USD (scale=2)
        assertEquals(new BigDecimal("108.00"), converted);
    }

    @Test
    void convert_eurToVnd_usesZeroScale() {
        BigDecimal converted = provider.convert(
                new BigDecimal("1.00"), Currency.EUR, Currency.VND);
        // 1 EUR = 27000 VND, but VND has scale=0 so no fractional digits.
        assertEquals(0, converted.scale(), "VND amounts must be whole numbers");
        assertEquals(new BigDecimal("27000"), converted);
    }
}
