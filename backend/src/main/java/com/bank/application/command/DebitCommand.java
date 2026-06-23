package com.bank.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Command to debit money from an account.
 */
@Getter
@Builder
public class DebitCommand {

    @NotNull
    private final Long accountId;

    @NotNull
    @Positive
    private final BigDecimal amount;
}
