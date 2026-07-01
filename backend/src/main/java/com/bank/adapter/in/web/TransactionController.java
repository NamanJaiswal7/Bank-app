package com.bank.adapter.in.web;

import com.bank.adapter.in.web.dto.ExchangeRequest;
import com.bank.adapter.in.web.dto.TransactionResponse;
import com.bank.application.command.ExchangeCommand;
import com.bank.application.port.in.ExchangeUseCase;
import com.bank.application.port.in.TransactionQueryUseCase;
import com.bank.domain.model.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final ExchangeUseCase exchangeUseCase;
    private final TransactionQueryUseCase transactionQueryUseCase;

    @PostMapping("/exchange")
    public List<TransactionResponse> exchange(@Valid @RequestBody ExchangeRequest request) {
        ExchangeCommand command = ExchangeCommand.builder()
                .sourceAccountId(request.getSourceAccountId())
                .targetAccountId(request.getTargetAccountId())
                .amount(request.getAmount())
                .build();
        return exchangeUseCase.exchange(command).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/transactions/{id}")
    public TransactionResponse getTransaction(@PathVariable @Positive Long id) {
        return toResponse(transactionQueryUseCase.getTransaction(id));
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).accountId(t.getAccountId())
                .type(t.getType().name())
                .amount(t.getAmount()).currency(t.getCurrency().name())
                .balanceAfter(t.getBalanceAfter())
                .description(t.getDescription())
                .referenceId(t.getReferenceId() != null ? t.getReferenceId().toString() : null)
                .timestamp(t.getTimestamp())
                .build();
    }
}
