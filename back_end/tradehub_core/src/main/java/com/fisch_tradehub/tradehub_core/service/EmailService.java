package com.fisch_tradehub.tradehub_core.service;

/**
 * Service interface for sending emails.
 */
public interface EmailService {
    
    /**
     * Send a password reset email.
     *
     * @param to the recipient email address
     * @param resetToken the password reset token
     */
    void sendPasswordResetEmail(String to, String resetToken);
}
