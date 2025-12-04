package com.fisch_tradehub.tradehub_core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
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
import com.fisch_tradehub.tradehub_core.repository.BillInfoRepository;
import com.fisch_tradehub.tradehub_core.repository.BillRepository;
import com.fisch_tradehub.tradehub_core.repository.CartRepository;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.BillDTO;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillInfoRepository billInfoRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BillService billService;

    // ========== Test Data Helpers ==========

    private Bill createBillWithStatus(Long id, BillStatus status) {
        User buyer = new User();
        buyer.setId(1L);
        buyer.setUsername("testuser");
        
        Bill bill = new Bill();
        bill.setId(id);
        bill.setStatus(status);
        bill.setBuyer(buyer);
        bill.setTotal(BigDecimal.valueOf(1000));
        return bill;
    }

    // ========== completeBill Tests ==========

    @Test
    @DisplayName("completeBill - Should complete when status is PROCESSING")
    void completeBill_ShouldComplete_WhenStatusProcessing() {
        // Arrange
        Bill bill = createBillWithStatus(1L, BillStatus.PROCESSING);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);
        when(billInfoRepository.findByBillId(1L)).thenReturn(List.of());

        // Act
        BillDTO result = billService.completeBill(1L);

        // Assert
        assertThat(result.status()).isEqualTo(BillStatus.COMPLETED);
        assertThat(bill.getClosedAt()).isNotNull();
        verify(billRepository).save(bill);
    }

    @Test
    @DisplayName("completeBill - Should throw when status is PENDING_PAYMENT")
    void completeBill_ShouldThrow_WhenStatusPendingPayment() {
        Bill bill = createBillWithStatus(1L, BillStatus.PENDING_PAYMENT);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() -> billService.completeBill(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Only processing bills can be completed");
    }

    @Test
    @DisplayName("completeBill - Should throw when status is COMPLETED")
    void completeBill_ShouldThrow_WhenAlreadyCompleted() {
        Bill bill = createBillWithStatus(1L, BillStatus.COMPLETED);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() -> billService.completeBill(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Only processing bills can be completed");
    }

    @Test
    @DisplayName("completeBill - Should throw when bill not found")
    void completeBill_ShouldThrow_WhenBillNotFound() {
        when(billRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.completeBill(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Bill not found");
    }

    // ========== getBillsByStatus Tests ==========

    @Test
    @DisplayName("getBillsByStatus - Should filter by status")
    void getBillsByStatus_ShouldFilterByStatus() {
        Bill bill1 = createBillWithStatus(1L, BillStatus.PROCESSING);
        Bill bill2 = createBillWithStatus(2L, BillStatus.PROCESSING);

        when(billRepository.findByStatus(BillStatus.PROCESSING))
            .thenReturn(List.of(bill1, bill2));
        when(billInfoRepository.findByBillId(any())).thenReturn(List.of());

        List<BillDTO> result = billService.getBillsByStatus(BillStatus.PROCESSING);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(b -> b.status() == BillStatus.PROCESSING);
    }

    // ========== adminCancelBill Tests ==========

    @Test
    @DisplayName("adminCancelBill - Should cancel PENDING_PAYMENT bill")
    void adminCancelBill_ShouldCancelPendingBill() {
        Bill bill = createBillWithStatus(1L, BillStatus.PENDING_PAYMENT);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);
        when(billInfoRepository.findByBillId(1L)).thenReturn(List.of());

        BillDTO result = billService.adminCancelBill(1L);

        assertThat(result.status()).isEqualTo(BillStatus.CANCELLED);
        verify(billRepository).save(bill);
    }

    @Test
    @DisplayName("adminCancelBill - Should cancel PROCESSING bill")
    void adminCancelBill_ShouldCancelProcessingBill() {
        Bill bill = createBillWithStatus(1L, BillStatus.PROCESSING);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);
        when(billInfoRepository.findByBillId(1L)).thenReturn(List.of());

        BillDTO result = billService.adminCancelBill(1L);

        assertThat(result.status()).isEqualTo(BillStatus.CANCELLED);
    }

    @Test
    @DisplayName("adminCancelBill - Should throw when bill is COMPLETED")
    void adminCancelBill_ShouldThrow_WhenBillCompleted() {
        Bill bill = createBillWithStatus(1L, BillStatus.COMPLETED);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() -> billService.adminCancelBill(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Invalid status transition");
    }
}
