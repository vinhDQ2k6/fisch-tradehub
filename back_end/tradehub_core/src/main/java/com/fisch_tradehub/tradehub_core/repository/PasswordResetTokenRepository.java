package com.fisch_tradehub.tradehub_core.repository;

import com.fisch_tradehub.tradehub_core.entity.PasswordResetToken;
import com.fisch_tradehub.tradehub_core.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for password reset tokens.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  Optional<PasswordResetToken> findByToken(String token);

  List<PasswordResetToken> findByUserAndUsedFalse(User user);
}
