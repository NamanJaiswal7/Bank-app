package com.bank.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error envelope returned from every failing endpoint.
 *
 * <p>The shape is intentionally stable so that API consumers (the Angular
 * frontend, monitoring tools, future SDKs) can rely on it. Fields:
 * <ul>
 *   <li>{@code timestamp} — when the error was produced (server clock, ISO-8601).</li>
 *   <li>{@code status}    — HTTP status code (mirrors the response status, for clients that strip it).</li>
 *   <li>{@code errorCode} — stable, machine-readable identifier (e.g. {@code INSUFFICIENT_FUNDS}).</li>
 *   <li>{@code message}   — human-readable description, safe to display to end users.</li>
 *   <li>{@code path}      — the request URI that produced the error.</li>
 *   <li>{@code errors}    — optional per-field validation errors, only present for
 *                          {@code VALIDATION_ERROR} responses. Keys are field names
 *                          (dot-notation for nested paths), values are messages.</li>
 * </ul>
 *
 * <p>Any {@code null} field is omitted from the JSON payload to keep responses
 * compact — see {@link JsonInclude.Include#NON_NULL}.</p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String errorCode;
    private String message;
    private String path;
    private Map<String, String> errors;
}
