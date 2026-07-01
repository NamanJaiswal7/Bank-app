package com.bank.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter @NoArgsConstructor @AllArgsConstructor
public class ExchangeRequest {
    @NotNull(message = "sourceAccountId is required")
    @Positive(message = "sourceAccountId must be a positive value")
    private Long sourceAccountId;

    @NotNull(message = "targetAccountId is required")
    @Positive(message = "targetAccountId must be a positive value")
    private Long targetAccountId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;
}
