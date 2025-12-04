package com.fisch_tradehub.tradehub_core.web.dto;

import com.fisch_tradehub.tradehub_core.entity.User;
import java.time.LocalDateTime;

/**
 * DTO for admin user management operations.
 * Contains user information visible to administrators.
 */
public record AdminUserDTO(
    Long id,
    String username,
    String email,
    String role,
    boolean active,
    LocalDateTime createdAt
) {
    /**
     * Create AdminUserDTO from User entity.
     *
     * @param user the user entity
     * @return AdminUserDTO representation
     */
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
