package com.fisch_tradehub.tradehub_core.service;

/**
 * Interface for email sending operations.
 */
public interface EmailService {

  /**
   * Send a password reset email.
   *
   * @param to the recipient email address
   * @param resetToken the reset token to include in the email
   */
  void sendPasswordResetEmail(String to, String resetToken);
}
