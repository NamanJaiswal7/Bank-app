package com.bank.domain.specification;

import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SufficientBalanceSpecificationTest {

    private static Account accountWith(String balance) {
        return Account.builder()
                .id(1L)
                .userId(1L)
                .currency(Currency.EUR)
                .balance(new BigDecimal(balance))
                .build();
    }

    @Test
    void satisfied_whenBalanceCoversAmount() {
        Specification<Account> spec = new SufficientBalanceSpecification(new BigDecimal("50"));
        assertTrue(spec.isSatisfiedBy(accountWith("100")));
        assertTrue(spec.isSatisfiedBy(accountWith("50")), "equal balance is still satisfied");
    }

    @Test
    void unsatisfied_whenBalanceBelowAmount() {
        Specification<Account> spec = new SufficientBalanceSpecification(new BigDecimal("100"));
        assertFalse(spec.isSatisfiedBy(accountWith("99.99")));
    }

    @Test
    void compositeAnd_requiresBothSides() {
        Specification<Account> coversTen = new SufficientBalanceSpecification(new BigDecimal("10"));
        Specification<Account> coversThousand = new SufficientBalanceSpecification(new BigDecimal("1000"));
        Specification<Account> both = coversTen.and(coversThousand);

        assertTrue(both.isSatisfiedBy(accountWith("1500")));
        assertFalse(both.isSatisfiedBy(accountWith("500")));
    }

    @Test
    void compositeOr_satisfiedWhenEitherSideHolds() {
        Specification<Account> coversTen = new SufficientBalanceSpecification(new BigDecimal("10"));
        Specification<Account> coversThousand = new SufficientBalanceSpecification(new BigDecimal("1000"));
        Specification<Account> either = coversTen.or(coversThousand);

        assertTrue(either.isSatisfiedBy(accountWith("500")), "covers the cheaper threshold");
        assertFalse(either.isSatisfiedBy(accountWith("5")));
    }

    @Test
    void compositeNot_invertsResult() {
        Specification<Account> coversHundred = new SufficientBalanceSpecification(new BigDecimal("100"));
        Specification<Account> doesNotCoverHundred = coversHundred.not();

        assertTrue(doesNotCoverHundred.isSatisfiedBy(accountWith("50")));
        assertFalse(doesNotCoverHundred.isSatisfiedBy(accountWith("200")));
    }
}
