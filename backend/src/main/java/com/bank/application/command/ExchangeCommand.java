package com.bank.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Command to perform a currency exchange between two accounts.
 */
@Getter
@Builder
public class ExchangeCommand {

    @NotNull
    private final Long sourceAccountId;

    @NotNull
    private final Long targetAccountId;

    @NotNull
    @Positive
    private final BigDecimal amount;
}
