package com.bank.domain.factory;

import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;
import com.bank.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Factory for creating Transaction instances.
 * Centralizes transaction construction to ensure consistency.
 */
public final class TransactionFactory {

    private TransactionFactory() {
        // Static factory — no instantiation
    }

    public static Transaction createCredit(Long accountId, BigDecimal amount,
                                           BigDecimal balanceAfter, Currency currency) {
        return Transaction.builder()
                .accountId(accountId)
                .type(TransactionType.CREDIT)
                .amount(amount)
                .currency(currency)
                .balanceAfter(balanceAfter)
                .description("Credit of " + currency.format(amount))
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static Transaction createDebit(Long accountId, BigDecimal amount,
                                          BigDecimal balanceAfter, Currency currency) {
        return Transaction.builder()
                .accountId(accountId)
                .type(TransactionType.DEBIT)
                .amount(amount)
                .currency(currency)
                .balanceAfter(balanceAfter)
                .description("Debit of " + currency.format(amount))
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a linked pair of exchange transactions sharing the same referenceId.
     *
     * @return list of exactly 2 transactions: [EXCHANGE_OUT on source, EXCHANGE_IN on target]
     */
    public static List<Transaction> createExchangePair(
            Long sourceAccountId, Long targetAccountId,
            BigDecimal sourceAmount, BigDecimal targetAmount,
            BigDecimal sourceBalanceAfter, BigDecimal targetBalanceAfter,
            Currency sourceCurrency, Currency targetCurrency) {

        UUID referenceId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Transaction outTx = Transaction.builder()
                .accountId(sourceAccountId)
                .type(TransactionType.EXCHANGE_OUT)
                .amount(sourceAmount)
                .currency(sourceCurrency)
                .balanceAfter(sourceBalanceAfter)
                .description("Exchange " + sourceCurrency.format(sourceAmount)
                        + " → " + targetCurrency.format(targetAmount))
                .referenceId(referenceId)
                .timestamp(now)
                .build();

        Transaction inTx = Transaction.builder()
                .accountId(targetAccountId)
                .type(TransactionType.EXCHANGE_IN)
                .amount(targetAmount)
                .currency(targetCurrency)
                .balanceAfter(targetBalanceAfter)
                .description("Exchange " + sourceCurrency.format(sourceAmount)
                        + " → " + targetCurrency.format(targetAmount))
                .referenceId(referenceId)
                .timestamp(now)
                .build();

        return List.of(outTx, inTx);
    }
}
