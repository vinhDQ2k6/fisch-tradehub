package com.fisch_tradehub.tradehub_core.payment;

import java.math.BigDecimal;

/**
 * Payment request data.
 */
public record PaymentRequest(
    Long billId,
    BigDecimal amount,
    String currency  // "VND" default
) {}
