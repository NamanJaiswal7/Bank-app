package com.bank.adapter.out.persistence.repository;

import com.bank.adapter.out.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {

    List<AccountJpaEntity> findByUserId(Long userId);
}
