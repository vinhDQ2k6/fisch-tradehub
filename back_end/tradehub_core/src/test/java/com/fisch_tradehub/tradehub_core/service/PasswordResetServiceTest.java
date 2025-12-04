package com.fisch_tradehub.tradehub_core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fisch_tradehub.tradehub_core.entity.PasswordResetToken;
import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.exception.BusinessException;
import com.fisch_tradehub.tradehub_core.repository.PasswordResetTokenRepository;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    // ========== Test Data Helpers ==========

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        return user;
    }

    private PasswordResetToken createToken(User user, boolean expired, boolean used) {
        return PasswordResetToken.builder()
            .id(1L)
            .token("test-token-123")
            .user(user)
            .expiryDate(expired
                ? LocalDateTime.now().minusHours(1)
                : LocalDateTime.now().plusHours(24))
            .used(used)
            .build();
    }

    // ========== createPasswordResetToken Tests ==========

    @Test
    @DisplayName("createToken - Should save token when email exists")
    void createToken_ShouldSaveToken_WhenEmailExists() {
        User user = createTestUser();
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

        passwordResetService.createPasswordResetToken("test@example.com");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    @DisplayName("createToken - Should not throw when email not exists (security)")
    void createToken_ShouldNotThrow_WhenEmailNotExists() {
        when(userRepository.findByEmail("unknown@example.com"))
            .thenReturn(Optional.empty());

        // Should not throw - security measure
        assertDoesNotThrow(() ->
            passwordResetService.createPasswordResetToken("unknown@example.com"));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    // ========== resetPassword Tests ==========

    @Test
    @DisplayName("resetPassword - Should update password when token valid")
    void resetPassword_ShouldUpdatePassword_WhenTokenValid() {
        User user = createTestUser();
        PasswordResetToken token = createToken(user, false, false);

        when(tokenRepository.findByToken("test-token-123"))
            .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword123"))
            .thenReturn("encodedPassword");

        passwordResetService.resetPassword("test-token-123", "newPassword123");

        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("resetPassword - Should throw when token not found")
    void resetPassword_ShouldThrow_WhenTokenNotFound() {
        when(tokenRepository.findByToken("invalid-token"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            passwordResetService.resetPassword("invalid-token", "newPassword"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Invalid or expired reset token");
    }

    @Test
    @DisplayName("resetPassword - Should throw when token expired")
    void resetPassword_ShouldThrow_WhenTokenExpired() {
        User user = createTestUser();
        PasswordResetToken expiredToken = createToken(user, true, false);

        when(tokenRepository.findByToken("expired-token"))
            .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() ->
            passwordResetService.resetPassword("expired-token", "newPassword"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Reset token has expired");
    }

    @Test
    @DisplayName("resetPassword - Should throw when token already used")
    void resetPassword_ShouldThrow_WhenTokenAlreadyUsed() {
        User user = createTestUser();
        PasswordResetToken usedToken = createToken(user, false, true);

        when(tokenRepository.findByToken("used-token"))
            .thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() ->
            passwordResetService.resetPassword("used-token", "newPassword"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Reset token has already been used");
    }
}
