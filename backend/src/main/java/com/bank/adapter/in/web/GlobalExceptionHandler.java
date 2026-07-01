package com.bank.adapter.in.web;

import com.bank.adapter.in.web.dto.ErrorResponse;
import com.bank.domain.exception.*;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps domain and infrastructure exceptions to HTTP responses with a stable
 * {@link ErrorResponse} envelope.
 *
 * <p>Status code mapping (see DEC-007):
 * <table>
 *   <tr><th>Exception</th><th>HTTP status</th></tr>
 *   <tr><td>{@link AccountNotFoundException}, {@link TransactionNotFoundException},
 *           {@link UserNotFoundException}, {@link NoResourceFoundException}</td><td>404 Not Found</td></tr>
 *   <tr><td>{@link InsufficientFundsException}, {@link CurrencyMismatchException},
 *           {@link SameAccountExchangeException}</td><td>422 Unprocessable Entity</td></tr>
 *   <tr><td>{@link ExternalValidationException}</td><td>502 Bad Gateway</td></tr>
 *   <tr><td>{@link ObjectOptimisticLockingFailureException}</td><td>409 Conflict</td></tr>
 *   <tr><td>{@link MethodArgumentNotValidException}, {@link ConstraintViolationException},
 *           {@link MethodArgumentTypeMismatchException}, {@link MissingServletRequestParameterException},
 *           {@link HttpMessageNotReadableException}, {@link InvalidCurrencyException},
 *           {@link IllegalArgumentException}</td><td>400 Bad Request</td></tr>
 *   <tr><td>{@link HttpRequestMethodNotSupportedException}</td><td>405 Method Not Allowed</td></tr>
 *   <tr><td>{@link HttpMediaTypeNotSupportedException}</td><td>415 Unsupported Media Type</td></tr>
 *   <tr><td>any other</td><td>500 Internal Server Error</td></tr>
 * </table>
 *
 * <p>Design notes:
 * <ul>
 *   <li>Domain exceptions carry their own {@code errorCode} — the handler just
 *       maps them to a status. Adding a new domain exception usually needs a
 *       single new handler here, nothing else.</li>
 *   <li>Framework/infra exceptions are translated to fixed error codes so that
 *       clients (see the Angular {@code errorInterceptor}) get a stable
 *       envelope regardless of Spring internals.</li>
 *   <li>The generic {@code Exception} fallback logs the full stack trace but
 *       never leaks internals to the client.</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------------------------------------------------------------------
    // Domain exceptions
    // ---------------------------------------------------------------------

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

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest req) {
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

    @ExceptionHandler(InvalidCurrencyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCurrency(
            InvalidCurrencyException ex, HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex, req);
    }

    @ExceptionHandler(ExternalValidationException.class)
    public ResponseEntity<ErrorResponse> handleExternalValidation(
            ExternalValidationException ex, HttpServletRequest req) {
        log.warn("External validation failed: {}", ex.getMessage());
        // 502 — we acted as a gateway to an upstream service that misbehaved.
        return buildResponse(HttpStatus.BAD_GATEWAY, ex, req);
    }

    /**
     * Fallback for any {@link DomainException} subtype that does not yet have
     * a dedicated handler. Prevents new domain exceptions from silently
     * degrading to a generic 500 — they surface as 422 with their own
     * error code until a more specific mapping is added.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(
            DomainException ex, HttpServletRequest req) {
        log.warn("Unmapped domain exception {}: {}", ex.getErrorCode(), ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex, req);
    }

    // ---------------------------------------------------------------------
    // Persistence / concurrency
    // ---------------------------------------------------------------------

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        log.warn("Optimistic lock conflict: {}", ex.getMessage());
        return respond(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION",
                "The account was modified by another request. Please retry.", req);
    }

    // ---------------------------------------------------------------------
    // Request validation (body / params / path)
    // ---------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                fe -> fieldErrors.put(fe.getField(),
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid value"));
        String summary = fieldErrors.isEmpty()
                ? "Validation failed"
                : "Validation failed for " + fieldErrors.size() + " field(s)";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .errorCode("VALIDATION_ERROR")
                        .message(summary)
                        .path(pathOf(req))
                        .errors(fieldErrors)
                        .build()
        );
    }

    /**
     * Triggered by {@code @Validated} constraints on path variables and
     * query parameters (e.g. {@code @Positive Long id}).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String path = v.getPropertyPath().toString();
            // Strip the "method.arg" prefix so clients see just the parameter name.
            String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            fieldErrors.put(field, v.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .errorCode("VALIDATION_ERROR")
                        .message("Request parameter validation failed")
                        .path(pathOf(req))
                        .errors(fieldErrors)
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "value";
        String message = String.format("Parameter '%s' must be of type %s", ex.getName(), expected);
        return respond(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", message, req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest req) {
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        return respond(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message, req);
    }

    /**
     * Fires when the request body is malformed JSON, contains an unknown
     * enum value (Jackson {@link InvalidFormatException}), or otherwise
     * cannot be deserialised.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && ife.getTargetType() != null
                && ife.getTargetType().isEnum()) {
            String field = ife.getPath().isEmpty() ? "value"
                    : ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            Object[] allowed = ife.getTargetType().getEnumConstants();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < allowed.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(allowed[i]);
            }
            String message = String.format("Field '%s' has an unsupported value '%s'. Allowed: %s",
                    field, ife.getValue(), sb);
            return respond(HttpStatus.BAD_REQUEST, "INVALID_ENUM_VALUE", message, req);
        }
        return respond(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST",
                "Request body is missing or malformed", req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                ex.getMessage() != null ? ex.getMessage() : "Bad request", req);
    }

    // ---------------------------------------------------------------------
    // Routing / method / media type
    // ---------------------------------------------------------------------

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(
            NoResourceFoundException ex, HttpServletRequest req) {
        return respond(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND",
                "No endpoint " + ex.getHttpMethod() + " " + ex.getResourcePath(), req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return respond(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "HTTP method " + ex.getMethod() + " is not supported for this endpoint", req);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return respond(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Content type " + ex.getContentType() + " is not supported", req);
    }

    // ---------------------------------------------------------------------
    // Last-resort fallback
    // ---------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {} {}",
                req != null ? req.getMethod() : "?", pathOf(req), ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred", req);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, DomainException ex, HttpServletRequest req) {
        return respond(status, ex.getErrorCode(), ex.getMessage(), req);
    }

    private ResponseEntity<ErrorResponse> respond(
            HttpStatus status, String errorCode, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .errorCode(errorCode)
                        .message(message)
                        .path(pathOf(req))
                        .build()
        );
    }

    private String pathOf(HttpServletRequest req) {
        return req != null ? req.getRequestURI() : null;
    }
}
