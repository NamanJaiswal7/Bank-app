package com.bank.application.port.out;

import com.bank.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for User persistence.
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    boolean existsById(Long id);
}
