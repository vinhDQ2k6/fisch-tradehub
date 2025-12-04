package com.fisch_tradehub.tradehub_core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisch_tradehub.tradehub_core.entity.PasswordResetToken;
import com.fisch_tradehub.tradehub_core.entity.User;

/**
 * Repository for password reset token operations.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    /**
     * Find a token by its value.
     *
     * @param token the token value
     * @return the token if found
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find all unused tokens for a user.
     *
     * @param user the user
     * @return list of unused tokens
     */
    List<PasswordResetToken> findByUserAndUsedFalse(User user);
}
