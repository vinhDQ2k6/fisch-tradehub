package com.fisch_tradehub.tradehub_core.payment;

/**
 * Represents the result of a payment processing request.
 */
public record PaymentResult(
    boolean success,
    String transactionId,
    String message,
    String gatewayName
) {}
