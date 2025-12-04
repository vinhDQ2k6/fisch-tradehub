package com.fisch_tradehub.tradehub_core.payment;

/**
 * Result of a payment processing attempt.
 */
public record PaymentResult(
    boolean success,
    String transactionId,
    String message,
    String gatewayName
) {}
