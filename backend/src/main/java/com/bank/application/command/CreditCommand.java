package com.bank.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Command to credit money to an account.
 */
@Getter
@Builder
public class CreditCommand {

    @NotNull
    private final Long accountId;

    @NotNull
    @Positive
    private final BigDecimal amount;
}
