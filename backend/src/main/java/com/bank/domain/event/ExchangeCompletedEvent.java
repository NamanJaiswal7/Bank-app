package com.bank.domain.event;

import com.bank.domain.model.Currency;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Raised when a currency exchange is completed between two accounts.
 */
@Getter
public class ExchangeCompletedEvent extends DomainEvent {

    private final Long sourceAccountId;
    private final Long targetAccountId;
    private final BigDecimal sourceAmount;
    private final BigDecimal targetAmount;
    private final Currency sourceCurrency;
    private final Currency targetCurrency;
    private final UUID referenceId;

    public ExchangeCompletedEvent(Long sourceAccountId, Long targetAccountId,
                                   BigDecimal sourceAmount, BigDecimal targetAmount,
                                   Currency sourceCurrency, Currency targetCurrency,
                                   UUID referenceId) {
        super();
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.sourceAmount = sourceAmount;
        this.targetAmount = targetAmount;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.referenceId = referenceId;
    }
}
