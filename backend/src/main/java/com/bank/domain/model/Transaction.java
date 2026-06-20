package com.bank.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction entity — immutable record of a financial operation.
 * Created only via TransactionFactory (never directly).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private Long id;

    private Long accountId;

    private TransactionType type;

    private BigDecimal amount;

    private Currency currency;

    private BigDecimal balanceAfter;

    private String description;

    /** Links paired exchange transactions (EXCHANGE_IN ↔ EXCHANGE_OUT). */
    private UUID referenceId;

    private LocalDateTime timestamp;
}
