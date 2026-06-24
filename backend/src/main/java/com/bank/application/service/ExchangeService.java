package com.bank.application.service;

import com.bank.application.command.ExchangeCommand;
import com.bank.application.handler.ExchangeHandler;
import com.bank.application.port.in.ExchangeUseCase;
import com.bank.application.port.out.TransactionRepositoryPort;
import com.bank.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implements {@link ExchangeUseCase} by delegating to {@link ExchangeHandler}.
 *
 * <p>Annotated with {@link Retryable} so a concurrent modification on either
 * the source or target account — surfaced as
 * {@link ObjectOptimisticLockingFailureException} — is transparently retried
 * with exponential back-off. After the retry budget is exhausted the
 * exception bubbles up and is mapped to HTTP 409 by
 * {@code GlobalExceptionHandler}.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ExchangeService implements ExchangeUseCase {

    private final ExchangeHandler exchangeHandler;
    private final TransactionRepositoryPort transactionRepository;

    @Override
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2.0))
    public List<Transaction> exchange(ExchangeCommand command) {
        List<Transaction> transactions = exchangeHandler.handle(command);
        return transactionRepository.saveAll(transactions);
    }
}
