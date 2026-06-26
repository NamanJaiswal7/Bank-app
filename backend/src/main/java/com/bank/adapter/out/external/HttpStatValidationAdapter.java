package com.bank.adapter.out.external;

import com.bank.application.port.out.ExternalValidationPort;
import com.bank.domain.exception.ExternalValidationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Adapter for external validation via httpstat.us.
 * Uses Circuit Breaker to prevent cascading failures.
 */
@Slf4j
@Component
public class HttpStatValidationAdapter implements ExternalValidationPort {

    private final RestTemplate restTemplate;
    private final String validationUrl;
    private final int timeoutMs;

    public HttpStatValidationAdapter(
            RestTemplate restTemplate,
            @Value("${external.validation.url}") String validationUrl,
            @Value("${external.validation.timeout-ms}") int timeoutMs) {
        this.restTemplate = restTemplate;
        this.validationUrl = validationUrl;
        this.timeoutMs = timeoutMs;
    }

    @Override
    @CircuitBreaker(name = "externalValidation", fallbackMethod = "fallback")
    public void validate(Long accountId) {
        log.debug("Calling external validation for account {} at {}", accountId, validationUrl);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(validationUrl, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ExternalValidationException(
                        "External validation returned status: " + response.getStatusCode());
            }

            log.debug("External validation succeeded for account {}", accountId);
        } catch (ExternalValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalValidationException(
                    "External validation failed for account " + accountId, e);
        }
    }

    /**
     * Circuit breaker fallback — allows the operation to proceed when the
     * external service is down. Logs a warning instead of blocking.
     */
    @SuppressWarnings("unused")
    private void fallback(Long accountId, Throwable t) {
        log.warn("Circuit breaker open for external validation. " +
                "Proceeding with debit for account {} without external validation. Cause: {}",
                accountId, t.getMessage());
    }
}
