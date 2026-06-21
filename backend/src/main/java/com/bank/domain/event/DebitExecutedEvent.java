package com.bank.domain.event;

import com.bank.domain.model.Currency;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Raised when money is debited from an account.
 */
@Getter
public class DebitExecutedEvent extends DomainEvent {

    private final Long accountId;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final Currency currency;

    public DebitExecutedEvent(Long accountId, BigDecimal amount,
                              BigDecimal balanceAfter, Currency currency) {
        super();
        this.accountId = accountId;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.currency = currency;
    }
}
