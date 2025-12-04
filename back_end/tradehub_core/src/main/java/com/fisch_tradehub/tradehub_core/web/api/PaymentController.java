package com.fisch_tradehub.tradehub_core.web.api;

import com.fisch_tradehub.tradehub_core.service.PaymentService;
import com.fisch_tradehub.tradehub_core.web.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for payment processing.
 * Authenticated users can pay for their own bills.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  /**
   * Process payment for a bill.
   *
   * @param billId the bill ID
   * @param userDetails the authenticated user
   * @return the payment response
   */
  @PostMapping("/{billId}")
  public ResponseEntity<PaymentResponse> processPayment(
      @PathVariable Long billId,
      @AuthenticationPrincipal UserDetails userDetails) {
    PaymentResponse response = paymentService.processPayment(
        billId,
        userDetails.getUsername()
    );
    return ResponseEntity.ok(response);
  }
}
