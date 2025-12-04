package com.fisch_tradehub.tradehub_core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fisch_tradehub.tradehub_core.entity.Bill;
import com.fisch_tradehub.tradehub_core.entity.BillStatus;
import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.exception.BusinessException;
import com.fisch_tradehub.tradehub_core.exception.ResourceNotFoundException;
import com.fisch_tradehub.tradehub_core.exception.UnauthorizedAccessException;
import com.fisch_tradehub.tradehub_core.payment.PaymentGateway;
import com.fisch_tradehub.tradehub_core.payment.PaymentResult;
import com.fisch_tradehub.tradehub_core.repository.BillRepository;
import com.fisch_tradehub.tradehub_core.web.dto.PaymentResponse;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private PaymentService paymentService;

    // ========== Test Data Helpers ==========

    private Bill createTestBill(Long id, String buyerUsername, BillStatus status) {
        User buyer = new User();
        buyer.setUsername(buyerUsername);

        Bill bill = new Bill();
        bill.setId(id);
        bill.setBuyer(buyer);
        bill.setStatus(status);
        bill.setTotal(new BigDecimal("1000000"));
        return bill;
    }

    // ========== processPayment Tests ==========

    @Test
    @DisplayName("processPayment - Should succeed when bill is valid")
    void processPayment_ShouldSucceed_WhenBillValid() {
        // Arrange
        Bill bill = createTestBill(1L, "user1", BillStatus.PENDING_PAYMENT);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentGateway.processPayment(any()))
            .thenReturn(new PaymentResult(true, "TXN-123", "OK", "Mock"));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);

        // Act
        PaymentResponse result = paymentService.processPayment(1L, "user1");

        // Assert
        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isEqualTo("TXN-123");
        assertThat(bill.getStatus()).isEqualTo(BillStatus.PROCESSING);
        verify(paymentGateway).processPayment(any());
    }

    @Test
    @DisplayName("processPayment - Should throw when not owner")
    void processPayment_ShouldThrow_WhenNotOwner() {
        Bill bill = createTestBill(1L, "user1", BillStatus.PENDING_PAYMENT);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() ->
            paymentService.processPayment(1L, "user2"))
            .isInstanceOf(UnauthorizedAccessException.class)
            .hasMessageContaining("You can only pay for your own bills");

        verify(paymentGateway, never()).processPayment(any());
    }

    @Test
    @DisplayName("processPayment - Should throw when bill already paid")
    void processPayment_ShouldThrow_WhenBillAlreadyPaid() {
        Bill bill = createTestBill(1L, "user1", BillStatus.PROCESSING);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() ->
            paymentService.processPayment(1L, "user1"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("not in PENDING_PAYMENT status");

        verify(paymentGateway, never()).processPayment(any());
    }

    @Test
    @DisplayName("processPayment - Should throw when bill not found")
    void processPayment_ShouldThrow_WhenBillNotFound() {
        when(billRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            paymentService.processPayment(999L, "user1"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Bill not found");
    }

    @Test
    @DisplayName("processPayment - Should not update bill when gateway fails")
    void processPayment_ShouldNotUpdateBill_WhenGatewayFails() {
        Bill bill = createTestBill(1L, "user1", BillStatus.PENDING_PAYMENT);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentGateway.processPayment(any()))
            .thenReturn(new PaymentResult(false, null, "Payment failed", "Mock"));

        PaymentResponse result = paymentService.processPayment(1L, "user1");

        assertThat(result.success()).isFalse();
        assertThat(bill.getStatus()).isEqualTo(BillStatus.PENDING_PAYMENT);
        verify(billRepository, never()).save(any());
    }
}
