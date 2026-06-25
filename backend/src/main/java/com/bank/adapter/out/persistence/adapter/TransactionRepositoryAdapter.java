package com.bank.adapter.out.persistence.adapter;

import com.bank.adapter.out.persistence.entity.TransactionJpaEntity;
import com.bank.adapter.out.persistence.repository.TransactionJpaRepository;
import com.bank.application.port.out.TransactionRepositoryPort;
import com.bank.domain.model.Currency;
import com.bank.domain.model.Transaction;
import com.bank.domain.model.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository jpaRepository;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = toEntity(transaction);
        TransactionJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Transaction> saveAll(List<Transaction> transactions) {
        List<TransactionJpaEntity> entities = transactions.stream()
                .map(this::toEntity).collect(Collectors.toList());
        return jpaRepository.saveAll(entities).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Transaction> findByAccountId(Long accountId, int page, int size) {
        return jpaRepository.findByAccountIdOrderByTimestampDesc(accountId, PageRequest.of(page, size))
                .getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByAccountId(Long accountId) {
        return jpaRepository.countByAccountId(accountId);
    }

    // --- Mapping ---

    private TransactionJpaEntity toEntity(Transaction domain) {
        return TransactionJpaEntity.builder()
                .id(domain.getId())
                .accountId(domain.getAccountId())
                .type(domain.getType().name())
                .amount(domain.getAmount())
                .currency(domain.getCurrency().name())
                .balanceAfter(domain.getBalanceAfter())
                .description(domain.getDescription())
                .referenceId(domain.getReferenceId() != null ? domain.getReferenceId().toString() : null)
                .timestamp(domain.getTimestamp())
                .build();
    }

    private Transaction toDomain(TransactionJpaEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .type(TransactionType.valueOf(entity.getType()))
                .amount(entity.getAmount())
                .currency(Currency.valueOf(entity.getCurrency()))
                .balanceAfter(entity.getBalanceAfter())
                .description(entity.getDescription())
                .referenceId(entity.getReferenceId() != null ? UUID.fromString(entity.getReferenceId()) : null)
                .timestamp(entity.getTimestamp())
                .build();
    }
}
