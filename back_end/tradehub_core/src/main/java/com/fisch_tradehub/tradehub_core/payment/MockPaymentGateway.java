package com.fisch_tradehub.tradehub_core.payment;

import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock payment gateway for development and testing.
 * Always returns successful payment result.
 */
@Component
@Primary  // Default gateway; remove @Primary when adding real gateway
@Slf4j
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing mock payment for bill: {}, amount: {}",
            request.billId(), request.amount());

        // Simulate 100% success rate
        String txnId = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentResult(
            true,
            txnId,
            "Payment successful (Mock)",
            getGatewayName()
        );
    }

    @Override
    public String getGatewayName() {
        return "MockGateway";
    }
}
