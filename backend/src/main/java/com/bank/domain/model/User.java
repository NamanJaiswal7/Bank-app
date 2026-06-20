package com.bank.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User domain model.
 *
 * <p>Represents an account holder. A user can own one or more {@link Account}s,
 * potentially in different currencies.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    /** Unique handle used for sign-in and display. */
    private String username;

    /** Contact email — used for notifications and identification. */
    private String email;

    private LocalDateTime createdAt;
}
