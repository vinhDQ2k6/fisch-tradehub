package com.fisch_tradehub.tradehub_core.web.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fisch_tradehub.tradehub_core.service.BillService;
import com.fisch_tradehub.tradehub_core.service.PayOSService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PayOSService payOSService;
  private final BillService billService;

  @PostMapping("/create-payment-link/{billId}")
  public ResponseEntity<Map<String, String>> createPaymentLink(
    @PathVariable Long billId
  ) {
    String checkoutUrl = payOSService.createPaymentLink(billId);
    return ResponseEntity.ok(Map.of("checkoutUrl", checkoutUrl));
  }

  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Payment Controller is working");
  }

  @GetMapping("/payos-webhook")
  public ResponseEntity<String> testWebhookGet() {
    return ResponseEntity.ok("Webhook GET is reachable. POST should work too.");
  }

  @PostMapping("/payos-webhook")
  public ResponseEntity<String> handlePayOSWebhook(
    @RequestBody ObjectNode webhookData
  ) {
    System.out.println("Received Webhook Data: " + webhookData);
    try {
      // Verify signature
      WebhookData verifiedData = payOSService.verifyWebhook(webhookData);

      if (verifiedData != null) {
        // Extract orderCode (which is billId)
        long orderCode = verifiedData.getOrderCode();

        // Confirm payment
        billService.confirmPayment(orderCode);

        return ResponseEntity.ok("Webhook processed successfully");
      }
    } catch (Exception e) {
      System.err.println("Webhook verification failed: " + e.getMessage());
      e.printStackTrace();
      // Return 200 to satisfy PayOS "Check Connection" even if verification fails
      return ResponseEntity.ok("Webhook received but verification failed");
    }

    return ResponseEntity.ok("Webhook received");
  }
}
