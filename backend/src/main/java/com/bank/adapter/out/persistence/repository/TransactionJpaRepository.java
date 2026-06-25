package com.bank.adapter.out.persistence.repository;

import com.bank.adapter.out.persistence.entity.TransactionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {

    Page<TransactionJpaEntity> findByAccountIdOrderByTimestampDesc(Long accountId, Pageable pageable);

    long countByAccountId(Long accountId);
}
