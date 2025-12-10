package com.fisch_tradehub.tradehub_core.payment;

import java.math.BigDecimal;

/**
 * Represents a payment request to a payment gateway.
 */
public record PaymentRequest(
    Long billId,
    BigDecimal amount,
    String currency  // "VND" default
) {}
