package com.fisch_tradehub.tradehub_core.web.dto;

import java.time.LocalDateTime;

import com.fisch_tradehub.tradehub_core.entity.User;

public record AdminUserDTO(
    Long id,
    String username,
    String email,
    String role,
    boolean active,
    LocalDateTime createdAt
) {
    public static AdminUserDTO from(User user) {
        return new AdminUserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.isActive(),
            user.getCreatedAt()
        );
    }
}
