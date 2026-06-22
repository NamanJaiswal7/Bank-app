package com.bank.domain.model;

import com.bank.domain.exception.CurrencyMismatchException;
import com.bank.domain.exception.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void testCreditSuccess() {
        Account account = Account.builder()
                .id(1L)
                .currency(Currency.USD)
                .balance(BigDecimal.TEN)
                .build();

        BigDecimal newBalance = account.credit(BigDecimal.valueOf(5));
        assertEquals(BigDecimal.valueOf(15), newBalance);
        assertEquals(BigDecimal.valueOf(15), account.getBalance());
        assertNotNull(account.getUpdatedAt());
    }

    @Test
    void testCreditInvalidAmount() {
        Account account = Account.builder()
                .id(1L)
                .currency(Currency.USD)
                .balance(BigDecimal.TEN)
                .build();

        assertThrows(IllegalArgumentException.class, () -> account.credit(null));
        assertThrows(IllegalArgumentException.class, () -> account.credit(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> account.credit(BigDecimal.valueOf(-5)));
    }

    @Test
    void testDebitSuccess() {
        Account account = Account.builder()
                .id(1L)
                .currency(Currency.USD)
                .balance(BigDecimal.TEN)
                .build();

        BigDecimal newBalance = account.debit(BigDecimal.valueOf(4));
        assertEquals(BigDecimal.valueOf(6), newBalance);
        assertEquals(BigDecimal.valueOf(6), account.getBalance());
        assertNotNull(account.getUpdatedAt());
    }

    @Test
    void testDebitInsufficientFunds() {
        Account account = Account.builder()
                .id(1L)
                .currency(Currency.USD)
                .balance(BigDecimal.TEN)
                .build();

        assertThrows(InsufficientFundsException.class, () -> account.debit(BigDecimal.valueOf(11)));
    }

    @Test
    void testDebitInvalidAmount() {
        Account account = Account.builder()
                .id(1L)
                .currency(Currency.USD)
                .balance(BigDecimal.TEN)
                .build();

        assertThrows(IllegalArgumentException.class, () -> account.debit(null));
        assertThrows(IllegalArgumentException.class, () -> account.debit(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> account.debit(BigDecimal.valueOf(-1)));
    }

    @Test
    void testValidateCurrency() {
        Account account = Account.builder()
                .id(1L)
                .currency(Currency.USD)
                .balance(BigDecimal.TEN)
                .build();

        assertDoesNotThrow(() -> account.validateCurrency(Currency.USD));
        assertThrows(CurrencyMismatchException.class, () -> account.validateCurrency(Currency.EUR));
    }

    @Test
    void testHasSufficientBalance() {
        Account account = Account.builder()
                .id(1L)
                .currency(Currency.USD)
                .balance(BigDecimal.TEN)
                .build();

        assertTrue(account.hasSufficientBalance(BigDecimal.TEN));
        assertTrue(account.hasSufficientBalance(BigDecimal.valueOf(5)));
        assertFalse(account.hasSufficientBalance(BigDecimal.valueOf(15)));
    }
}
