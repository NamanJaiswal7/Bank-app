package com.bank.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Builder
public class TransactionResponse {
    private Long id;
    private Long accountId;
    private String type;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balanceAfter;
    private String description;
    private String referenceId;
    private LocalDateTime timestamp;
}
