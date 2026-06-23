package com.bank.application.port.out;

/**
 * Outbound port for external validation (e.g., httpstat.us).
 * Called before debit operations to simulate external logging/validation.
 *
 * Adapter pattern: implementation handles HTTP, circuit breaker, timeouts.
 * Domain/application layer only knows this interface.
 */
public interface ExternalValidationPort {

    /**
     * Validates an operation externally. Called before debit.
     *
     * @param accountId the account being debited
     * @throws com.bank.domain.exception.ExternalValidationException if validation fails
     */
    void validate(Long accountId);
}
