package com.bank.adapter.out.persistence.adapter;

import com.bank.adapter.out.persistence.entity.AccountJpaEntity;
import com.bank.adapter.out.persistence.repository.AccountJpaRepository;
import com.bank.application.port.out.AccountRepositoryPort;
import com.bank.domain.model.Account;
import com.bank.domain.model.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter — implements AccountRepositoryPort by translating between domain and JPA.
 */
@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final AccountJpaRepository jpaRepository;

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = toEntity(account);
        AccountJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Account> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    // --- Mapping ---

    private AccountJpaEntity toEntity(Account domain) {
        return AccountJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .currency(domain.getCurrency().name())
                .balance(domain.getBalance())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private Account toDomain(AccountJpaEntity entity) {
        return Account.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .currency(Currency.valueOf(entity.getCurrency()))
                .balance(entity.getBalance())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
