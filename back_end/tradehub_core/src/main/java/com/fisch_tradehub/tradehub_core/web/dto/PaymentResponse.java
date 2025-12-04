package com.fisch_tradehub.tradehub_core.web.dto;

import com.fisch_tradehub.tradehub_core.entity.BillStatus;

/**
 * Response DTO for payment processing results.
 */
public record PaymentResponse(
    boolean success,
    String transactionId,
    String message,
    String gatewayName,
    Long billId,
    BillStatus newStatus
) {}
