package com.fisch_tradehub.tradehub_core.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Stub implementation of EmailService that logs emails instead of sending them.
 * For development and testing purposes.
 */
@Service
@Slf4j
public class StubEmailService implements EmailService {

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        // Log to console instead of sending real email
        log.info("========================================");
        log.info("       PASSWORD RESET EMAIL");
        log.info("========================================");
        log.info("To: {}", to);
        log.info("Reset Link: http://localhost:5173/reset-password?token={}", resetToken);
        log.info("Token expires in 24 hours");
        log.info("========================================");
    }
}
