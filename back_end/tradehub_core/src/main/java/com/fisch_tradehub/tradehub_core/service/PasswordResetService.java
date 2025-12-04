package com.fisch_tradehub.tradehub_core.service;

import com.fisch_tradehub.tradehub_core.common.Constants;
import com.fisch_tradehub.tradehub_core.entity.PasswordResetToken;
import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.exception.BusinessException;
import com.fisch_tradehub.tradehub_core.repository.PasswordResetTokenRepository;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for password reset operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  private static final int TOKEN_EXPIRY_HOURS = 24;

  /**
   * Create a password reset token and send email.
   * Does not throw error if email doesn't exist (security measure).
   *
   * @param email the user's email address
   */
  @Transactional
  public void createPasswordResetToken(String email) {
    Optional<User> userOptional = userRepository.findByEmail(email);

    // Security: don't reveal if email exists
    if (userOptional.isEmpty()) {
      log.warn("Password reset requested for non-existent email: {}", email);
      return;
    }

    User user = userOptional.get();
    String token = UUID.randomUUID().toString();

    PasswordResetToken resetToken = PasswordResetToken.builder()
        .token(token)
        .user(user)
        .expiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
        .used(false)
        .build();

    tokenRepository.save(resetToken);
    emailService.sendPasswordResetEmail(email, token);

    log.info("Password reset token created for user: {}", user.getUsername());
  }

  /**
   * Reset password using a valid token.
   *
   * @param token the reset token
   * @param newPassword the new password
   * @throws BusinessException if token is invalid, expired, or already used
   */
  @Transactional
  public void resetPassword(String token, String newPassword) {
    PasswordResetToken resetToken = tokenRepository.findByToken(token)
        .orElseThrow(() -> new BusinessException(Constants.TOKEN_NOT_FOUND));

    if (resetToken.isExpired()) {
      throw new BusinessException(Constants.TOKEN_EXPIRED);
    }

    if (resetToken.isUsed()) {
      throw new BusinessException(Constants.TOKEN_ALREADY_USED);
    }

    // Update password
    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    // Mark token as used
    resetToken.setUsed(true);
    tokenRepository.save(resetToken);

    log.info("Password reset successful for user: {}", user.getUsername());
  }
}
