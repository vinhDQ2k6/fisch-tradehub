package com.fisch_tradehub.tradehub_core.payment;

/**
 * Strategy interface for payment gateways.
 * Implementations can be MockPaymentGateway, VNPayGateway, etc.
 */
public interface PaymentGateway {

  /**
   * Process a payment request.
   *
   * @param request the payment request
   * @return the result of the payment processing
   */
  PaymentResult processPayment(PaymentRequest request);

  /**
   * Get the name of this payment gateway.
   *
   * @return the gateway name
   */
  String getGatewayName();
}
