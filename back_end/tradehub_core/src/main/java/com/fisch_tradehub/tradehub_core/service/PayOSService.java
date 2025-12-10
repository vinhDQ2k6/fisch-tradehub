package com.fisch_tradehub.tradehub_core.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fisch_tradehub.tradehub_core.entity.Bill;
import com.fisch_tradehub.tradehub_core.exception.BusinessException;
import com.fisch_tradehub.tradehub_core.exception.ResourceNotFoundException;
import com.fisch_tradehub.tradehub_core.repository.BillRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSService {

  @Value("${payos.client-id}")
  private String clientId;

  @Value("${payos.api-key}")
  private String apiKey;

  @Value("${payos.checksum-key}")
  private String checksumKey;

  private final BillRepository billRepository;
  private PayOS payOS;

  @jakarta.annotation.PostConstruct
  public void init() {
    log.info("Initializing PayOS with Client ID: {}", clientId);
    this.payOS = new PayOS(clientId, apiKey, checksumKey);
  }

  public String createPaymentLink(Long billId) {
    try {
      Bill bill = billRepository
        .findById(billId)
        .orElseThrow(() ->
          new ResourceNotFoundException("Bill not found with id: " + billId)
        );

      String description = "Thanh toan don hang " + bill.getId();

      // Map Bill items to PayOS items
      List<PaymentLinkItem> items = bill
        .getBillInfos()
        .stream()
        .map(info ->
          PaymentLinkItem.builder()
            .name(info.getFish().getName())
            .quantity(info.getAmount())
            .price(info.getPrice().longValue())
            .build()
        )
        .collect(Collectors.toList());

      CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
        .orderCode(bill.getId()) // Use Bill ID as Order Code (must be long)
        .amount(bill.getTotal().longValue())
        .description(description)
        .returnUrl("http://localhost:5173/payment/success")
        .cancelUrl("http://localhost:5173/payment/cancel")
        .items(items)
        .build();

      CreatePaymentLinkResponse data = payOS
        .paymentRequests()
        .create(paymentData);
      return data.getCheckoutUrl();
    } catch (Exception e) {
      log.error("Failed to create PayOS payment link", e);
      throw new BusinessException(
        "Could not create payment link: " + e.getMessage()
      );
    }
  }

  public PaymentLink getPaymentLinkInfo(Long orderCode) {
    try {
      return payOS.paymentRequests().get(orderCode);
    } catch (Exception e) {
      log.error("Failed to get PayOS payment info", e);
      return null;
    }
  }

  public WebhookData verifyWebhook(ObjectNode webhookData) {
    try {
      return payOS.webhooks().verify(webhookData);
    } catch (Exception e) {
      log.error("Webhook verification failed", e);
      throw new BusinessException("Invalid webhook signature");
    }
  }
}
