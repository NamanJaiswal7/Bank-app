package com.bank.domain.event;

import com.bank.domain.model.Currency;
import lombok.Getter;

/**
 * Raised when a new account is created.
 */
@Getter
public class AccountCreatedEvent extends DomainEvent {

    private final Long accountId;
    private final Long userId;
    private final Currency currency;

    public AccountCreatedEvent(Long accountId, Long userId, Currency currency) {
        super();
        this.accountId = accountId;
        this.userId = userId;
        this.currency = currency;
    }
}
