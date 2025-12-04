package com.fisch_tradehub.tradehub_core.web.dto;

import com.fisch_tradehub.tradehub_core.entity.BillStatus;

/**
 * Response for payment processing.
 */
public record PaymentResponse(
    boolean success,
    String transactionId,
    String message,
    String gatewayName,
    Long billId,
    BillStatus newStatus
) {}
