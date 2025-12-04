package com.fisch_tradehub.tradehub_core.payment;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Mock payment gateway for development/testing.
 * Always succeeds with a generated transaction ID.
 */
@Component
@Primary  // Use Mock by default; remove when adding real gateway
@Slf4j
public class MockPaymentGateway implements PaymentGateway {

  @Override
  public PaymentResult processPayment(PaymentRequest request) {
    log.info("Processing mock payment for bill: {}, amount: {}",
        request.billId(), request.amount());

    // Simulate 100% success
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
