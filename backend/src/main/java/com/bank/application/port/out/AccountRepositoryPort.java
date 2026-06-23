package com.bank.application.port.out;

import com.bank.domain.model.Account;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for Account persistence.
 * Implemented by infrastructure adapter (AccountRepositoryAdapter).
 */
public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findById(Long id);

    List<Account> findByUserId(Long userId);

    boolean existsById(Long id);
}
