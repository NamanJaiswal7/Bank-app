package com.bank.domain.event;

import com.bank.domain.model.Currency;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Raised when money is credited to an account.
 */
@Getter
public class AccountCreditedEvent extends DomainEvent {

    private final Long accountId;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final Currency currency;

    public AccountCreditedEvent(Long accountId, BigDecimal amount,
                                BigDecimal balanceAfter, Currency currency) {
        super();
        this.accountId = accountId;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.currency = currency;
    }
}
