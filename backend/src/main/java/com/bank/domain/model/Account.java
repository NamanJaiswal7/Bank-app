package com.bank.domain.model;

import com.bank.domain.exception.CurrencyMismatchException;
import com.bank.domain.exception.InsufficientFundsException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account aggregate root — rich domain model.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>Balance can never go negative ({@link InsufficientFundsException}).</li>
 *   <li>Amounts must be strictly positive.</li>
 *   <li>State mutations only happen through behaviour methods
 *       ({@link #credit(BigDecimal)}, {@link #debit(BigDecimal)}). The balance
 *       has no public setter to keep the aggregate's invariants safe.</li>
 * </ul>
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private Long id;

    private Long userId;

    private Currency currency;

    private BigDecimal balance;

    private Long version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Opens a new account for a user with a zero starting balance.
     *
     * <p>Use this factory method instead of the builder when constructing a
     * fresh account — it guarantees the initial invariants (zero balance,
     * timestamps set) are correct.</p>
     */
    public static Account open(Long userId, Currency currency) {
        LocalDateTime now = LocalDateTime.now();
        return Account.builder()
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Credits (adds) money to this account.
     *
     * @param amount the amount to credit (must be positive)
     * @return the new balance after credit
     * @throws IllegalArgumentException if amount is null or non-positive
     */
    public BigDecimal credit(BigDecimal amount) {
        validateAmount(amount);
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
        return this.balance;
    }

    /**
     * Debits (removes) money from this account.
     *
     * @param amount the amount to debit (must be positive)
     * @return the new balance after debit
     * @throws IllegalArgumentException if amount is null or non-positive
     * @throws InsufficientFundsException if balance would go negative
     */
    public BigDecimal debit(BigDecimal amount) {
        validateAmount(amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(this.id, this.balance, amount, this.currency);
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
        return this.balance;
    }

    /**
     * Validates that an amount used in a transaction matches this account's currency.
     *
     * @param transactionCurrency the currency of the incoming transaction
     * @throws CurrencyMismatchException if currencies don't match
     */
    public void validateCurrency(Currency transactionCurrency) {
        if (this.currency != transactionCurrency) {
            throw new CurrencyMismatchException(this.currency, transactionCurrency);
        }
    }

    /**
     * Checks if the account has sufficient balance for a given amount.
     *
     * @param amount the amount to check
     * @return true if balance &gt;= amount
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive, got: " + amount);
        }
    }
}
