package com.bank.domain.factory;

import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;
import com.bank.domain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionFactoryTest {

    @Test
    void createCredit_buildsCreditTransactionWithExpectedFields() {
        Transaction tx = TransactionFactory.createCredit(
                42L, new BigDecimal("100.00"), new BigDecimal("250.00"), Currency.EUR);

        assertEquals(42L, tx.getAccountId());
        assertEquals(TransactionType.CREDIT, tx.getType());
        assertEquals(new BigDecimal("100.00"), tx.getAmount());
        assertEquals(new BigDecimal("250.00"), tx.getBalanceAfter());
        assertEquals(Currency.EUR, tx.getCurrency());
        assertNotNull(tx.getTimestamp());
        assertNull(tx.getReferenceId(), "Non-exchange transactions must not have a referenceId");
        assertTrue(tx.getDescription().contains("Credit"));
    }

    @Test
    void createDebit_buildsDebitTransactionWithExpectedFields() {
        Transaction tx = TransactionFactory.createDebit(
                7L, new BigDecimal("50.00"), new BigDecimal("950.00"), Currency.USD);

        assertEquals(TransactionType.DEBIT, tx.getType());
        assertEquals(Currency.USD, tx.getCurrency());
        assertEquals(new BigDecimal("950.00"), tx.getBalanceAfter());
        assertNull(tx.getReferenceId());
    }

    @Test
    void createExchangePair_producesTwoLinkedTransactions() {
        List<Transaction> pair = TransactionFactory.createExchangePair(
                1L, 2L,
                new BigDecimal("100.00"), new BigDecimal("108.00"),
                new BigDecimal("400.00"), new BigDecimal("608.00"),
                Currency.EUR, Currency.USD);

        assertEquals(2, pair.size());

        Transaction out = pair.get(0);
        Transaction in = pair.get(1);

        assertEquals(TransactionType.EXCHANGE_OUT, out.getType());
        assertEquals(TransactionType.EXCHANGE_IN, in.getType());

        assertEquals(1L, out.getAccountId());
        assertEquals(2L, in.getAccountId());

        assertEquals(Currency.EUR, out.getCurrency());
        assertEquals(Currency.USD, in.getCurrency());

        assertEquals(new BigDecimal("100.00"), out.getAmount());
        assertEquals(new BigDecimal("108.00"), in.getAmount());

        // Critical invariant: both legs share the same referenceId so they can
        // be reconstructed as a logical "exchange" later.
        assertNotNull(out.getReferenceId());
        assertEquals(out.getReferenceId(), in.getReferenceId());

        // Both legs are timestamped together.
        assertEquals(out.getTimestamp(), in.getTimestamp());
    }
}
