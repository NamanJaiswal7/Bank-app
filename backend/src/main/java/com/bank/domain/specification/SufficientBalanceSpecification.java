package com.bank.domain.specification;

import com.bank.domain.model.Account;

import java.math.BigDecimal;

/**
 * Checks whether an account has sufficient balance for a given debit amount.
 */
public class SufficientBalanceSpecification extends AbstractSpecification<Account> {

    private final BigDecimal requiredAmount;

    public SufficientBalanceSpecification(BigDecimal requiredAmount) {
        this.requiredAmount = requiredAmount;
    }

    @Override
    public boolean isSatisfiedBy(Account account) {
        return account.getBalance().compareTo(requiredAmount) >= 0;
    }
}
