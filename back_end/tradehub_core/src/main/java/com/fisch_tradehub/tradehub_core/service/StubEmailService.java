package com.fisch_tradehub.tradehub_core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Stub implementation of EmailService.
 * Logs email content to console instead of actually sending.
 * Replace with real implementation (e.g., JavaMailSender) in production.
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
