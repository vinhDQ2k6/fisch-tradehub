package com.fisch_tradehub.tradehub_core.payment;

/**
 * Strategy interface for payment processing.
 * Implementations can support different payment gateways (Mock, VNPay, etc.)
 */
public interface PaymentGateway {
    
    /**
     * Process a payment request.
     *
     * @param request the payment request details
     * @return the payment result
     */
    PaymentResult processPayment(PaymentRequest request);
    
    /**
     * Get the name of this payment gateway.
     *
     * @return the gateway name
     */
    String getGatewayName();
}
