package com.bank.domain.exception;

import com.bank.domain.model.Currency;

import java.math.BigDecimal;

/**
 * Thrown when a debit or withdrawal is attempted on an account whose balance
 * is lower than the requested amount.
 *
 * <p>This exception is raised by the {@code Account} aggregate when its
 * invariant {@code balance >= debitAmount} would be violated.</p>
 *
 * @see com.bank.domain.model.Account#debit(BigDecimal)
 */
public class InsufficientFundsException extends DomainException {

    private static final String ERROR_CODE = "INSUFFICIENT_FUNDS";

    private final Long accountId;
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;
    private final Currency currency;

    /**
     * Constructs a new {@code InsufficientFundsException}.
     *
     * @param accountId       the ID of the account that lacks sufficient funds
     * @param currentBalance  the current balance of the account
     * @param requestedAmount the amount that was requested for withdrawal/debit
     * @param currency        the currency of the account
     */
    public InsufficientFundsException(Long accountId,
                                      BigDecimal currentBalance,
                                      BigDecimal requestedAmount,
                                      Currency currency) {
        super(ERROR_CODE, String.format(
                "Account %d has insufficient funds: current balance is %s but %s was requested (currency: %s)",
                accountId,
                currency.format(currentBalance),
                currency.format(requestedAmount),
                currency.name()));
        this.accountId = accountId;
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
        this.currency = currency;
    }

    /**
     * @return the ID of the account that lacks sufficient funds
     */
    public Long getAccountId() {
        return accountId;
    }

    /**
     * @return the account balance at the time of the failed operation
     */
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    /**
     * @return the amount that was requested but could not be fulfilled
     */
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    /**
     * @return the currency of the account
     */
    public Currency getCurrency() {
        return currency;
    }
}
