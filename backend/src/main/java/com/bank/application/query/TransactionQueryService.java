package com.bank.application.query;

import com.bank.application.port.in.TransactionQueryUseCase;
import com.bank.application.port.out.TransactionRepositoryPort;
import com.bank.domain.exception.TransactionNotFoundException;
import com.bank.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CQRS read side — handles transaction queries directly via repository port.
 * No domain logic, no validation — just reads.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryService implements TransactionQueryUseCase {

    private final TransactionRepositoryPort transactionRepository;

    @Override
    public Transaction getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    @Override
    public List<Transaction> getTransactionsByAccount(Long accountId, int page, int size) {
        return transactionRepository.findByAccountId(accountId, page, size);
    }

    @Override
    public long countTransactionsByAccount(Long accountId) {
        return transactionRepository.countByAccountId(accountId);
    }
}
