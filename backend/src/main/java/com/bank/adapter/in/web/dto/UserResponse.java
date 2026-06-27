package com.bank.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Transport-layer view of a {@link com.bank.domain.model.User}.
 *
 * <p>Returning a dedicated DTO (instead of leaking the domain model) prevents
 * future domain changes from breaking API consumers, and lets us tailor the
 * payload — e.g. to omit sensitive fields if/when they are added.</p>
 */
@Getter @Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}
