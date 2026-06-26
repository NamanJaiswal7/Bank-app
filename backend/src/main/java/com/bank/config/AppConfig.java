package com.bank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * General application configuration.
 *
 * <p>The {@link RestTemplate} bean is constructed with explicit connect/read
 * timeouts driven by the {@code external.validation.timeout-ms} property, so
 * a stuck or slow upstream cannot block the calling thread indefinitely —
 * particularly important since this same RestTemplate is wrapped by a
 * Resilience4j circuit breaker.</p>
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            @Value("${external.validation.timeout-ms}") int timeoutMs) {
        Duration timeout = Duration.ofMillis(timeoutMs);
        return builder
                .setConnectTimeout(timeout)
                .setReadTimeout(timeout)
                .build();
    }
}
