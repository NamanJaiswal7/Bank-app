package com.bank.adapter.in.web;

import com.bank.adapter.in.web.dto.*;
import com.bank.application.command.CreditCommand;
import com.bank.application.command.DebitCommand;
import com.bank.application.port.in.AccountUseCase;
import com.bank.application.port.in.TransactionQueryUseCase;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountUseCase accountUseCase;
    private final TransactionQueryUseCase transactionQueryUseCase;

    @GetMapping("/users/{userId}/accounts")
    public List<AccountResponse> getUserAccounts(@PathVariable @Positive Long userId) {
        return accountUseCase.getAccountsByUser(userId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @PostMapping("/users/{userId}/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody CreateAccountRequest request) {
        // Currency.fromString throws InvalidCurrencyException (400 with a stable
        // error code) rather than the raw IllegalArgumentException from valueOf().
        Currency currency = Currency.fromString(request.getCurrency());
        Account account = accountUseCase.createAccount(userId, currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(account));
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse getAccount(@PathVariable @Positive Long id) {
        return toResponse(accountUseCase.getAccount(id));
    }

    @PostMapping("/accounts/{id}/credit")
    public TransactionResponse credit(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CreditRequest request) {
        CreditCommand command = CreditCommand.builder()
                .accountId(id).amount(request.getAmount()).build();
        return toResponse(accountUseCase.credit(command));
    }

    @PostMapping("/accounts/{id}/debit")
    public TransactionResponse debit(
            @PathVariable @Positive Long id,
            @Valid @RequestBody DebitRequest request) {
        DebitCommand command = DebitCommand.builder()
                .accountId(id).amount(request.getAmount()).build();
        return toResponse(accountUseCase.debit(command));
    }

    @GetMapping("/accounts/{id}/transactions")
    public TransactionPageResponse getTransactions(
            @PathVariable @Positive Long id,
            @RequestParam(defaultValue = "0") @jakarta.validation.constraints.PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Positive int size) {
        List<Transaction> transactions = transactionQueryUseCase
                .getTransactionsByAccount(id, page, size);
        long total = transactionQueryUseCase.countTransactionsByAccount(id);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        // Empty result set (page 0 with 0 elements) is also "last" — mirrors
        // Spring Data Page#isLast so the frontend can stop polling.
        boolean last = totalPages == 0 || page >= totalPages - 1;

        return TransactionPageResponse.builder()
                .content(transactions.stream().map(this::toResponse).collect(Collectors.toList()))
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .last(last)
                .build();
    }

    // --- Mapping helpers ---

    private AccountResponse toResponse(Account a) {
        return AccountResponse.builder()
                .id(a.getId()).userId(a.getUserId())
                .currency(a.getCurrency().name())
                .balance(a.getBalance())
                .createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt())
                .build();
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
