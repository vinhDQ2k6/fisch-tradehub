package com.fisch_tradehub.tradehub_core.service;

import com.fisch_tradehub.tradehub_core.common.Constants;
import com.fisch_tradehub.tradehub_core.entity.Bill;
import com.fisch_tradehub.tradehub_core.entity.BillStatus;
import com.fisch_tradehub.tradehub_core.exception.BusinessException;
import com.fisch_tradehub.tradehub_core.exception.ResourceNotFoundException;
import com.fisch_tradehub.tradehub_core.exception.UnauthorizedAccessException;
import com.fisch_tradehub.tradehub_core.payment.PaymentGateway;
import com.fisch_tradehub.tradehub_core.payment.PaymentRequest;
import com.fisch_tradehub.tradehub_core.payment.PaymentResult;
import com.fisch_tradehub.tradehub_core.repository.BillRepository;
import com.fisch_tradehub.tradehub_core.web.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for processing payments through configured payment gateway.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  private final PaymentGateway paymentGateway;
  private final BillRepository billRepository;

  /**
   * Process payment for a bill.
   *
   * @param billId the bill ID
   * @param username the username of the payer
   * @return the payment response
   * @throws ResourceNotFoundException if bill not found
   * @throws UnauthorizedAccessException if user doesn't own the bill
   * @throws BusinessException if bill is not in PENDING_PAYMENT status
   */
  @Transactional
  public PaymentResponse processPayment(Long billId, String username) {
    // 1. Find bill
    Bill bill = billRepository.findById(billId)
        .orElseThrow(() -> new ResourceNotFoundException(Constants.BILL_NOT_FOUND));

    // 2. Validate ownership
    if (!bill.getBuyer().getUsername().equals(username)) {
      throw new UnauthorizedAccessException("You can only pay for your own bills");
    }

    // 3. Validate status
    if (bill.getStatus() != BillStatus.PENDING_PAYMENT) {
      throw new BusinessException("Bill is not in PENDING_PAYMENT status");
    }

    // 4. Create payment request
    PaymentRequest request = new PaymentRequest(
        billId,
        bill.getTotal(),
        "VND"
    );

    // 5. Process payment via gateway
    PaymentResult result = paymentGateway.processPayment(request);

    // 6. Update bill if success
    if (result.success()) {
      bill.setStatus(BillStatus.PROCESSING);
      bill.setTransactionId(result.transactionId());
      billRepository.save(bill);
      log.info("Payment successful. Bill {} marked as PROCESSING", billId);
    }

    return new PaymentResponse(
        result.success(),
        result.transactionId(),
        result.message(),
        result.gatewayName(),
        billId,
        bill.getStatus()
    );
  }
}
