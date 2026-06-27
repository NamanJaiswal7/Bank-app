package com.bank.adapter.in.web;

import com.bank.adapter.in.web.dto.ErrorResponse;
import com.bank.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Maps domain and infrastructure exceptions to HTTP responses with a stable
 * {@link ErrorResponse} envelope.
 *
 * <p>Status code mapping (see DEC-007):
 * <table>
 *   <tr><th>Exception</th><th>HTTP status</th></tr>
 *   <tr><td>{@link AccountNotFoundException}, {@link TransactionNotFoundException}</td><td>404 Not Found</td></tr>
 *   <tr><td>{@link InsufficientFundsException}, {@link CurrencyMismatchException},
 *           {@link SameAccountExchangeException}</td><td>422 Unprocessable Entity</td></tr>
 *   <tr><td>{@link ExternalValidationException}</td><td>502 Bad Gateway</td></tr>
 *   <tr><td>{@link ObjectOptimisticLockingFailureException}</td><td>409 Conflict</td></tr>
 *   <tr><td>{@link MethodArgumentNotValidException}, {@link IllegalArgumentException}</td><td>400 Bad Request</td></tr>
 *   <tr><td>any other</td><td>500 Internal Server Error</td></tr>
 * </table>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(
            AccountNotFoundException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.NOT_FOUND, ex, req);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFound(
            TransactionNotFoundException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.NOT_FOUND, ex, req);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(
            InsufficientFundsException ex, HttpServletRequest req) {
        // 422 — request was well-formed but a business rule blocks completion.
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex, req);
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyMismatch(
            CurrencyMismatchException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex, req);
    }

    @ExceptionHandler(SameAccountExchangeException.class)
    public ResponseEntity<ErrorResponse> handleSameAccountExchange(
            SameAccountExchangeException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex, req);
    }

    @ExceptionHandler(ExternalValidationException.class)
    public ResponseEntity<ErrorResponse> handleExternalValidation(
            ExternalValidationException ex, HttpServletRequest req) {
        log.warn("External validation failed: {}", ex.getMessage());
        // 502 — we acted as a gateway to an upstream service that misbehaved.
        return buildResponse(HttpStatus.BAD_GATEWAY, ex, req);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        log.warn("Optimistic lock conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                buildBody(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION",
                        "The account was modified by another request. Please retry.", req)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                buildBody(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                buildBody(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, req)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}", req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                        "An unexpected error occurred", req)
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, DomainException ex, HttpServletRequest req) {
        return ResponseEntity.status(status).body(
                buildBody(status, ex.getErrorCode(), ex.getMessage(), req)
        );
    }

    private ErrorResponse buildBody(
            HttpStatus status, String errorCode, String message, HttpServletRequest req) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .path(req != null ? req.getRequestURI() : null)
                .build();
    }
}
