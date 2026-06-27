package com.bank.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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
 * </ul>
 */
@Getter @Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String errorCode;
    private String message;
    private String path;
}
