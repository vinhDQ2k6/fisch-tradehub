package com.fisch_tradehub.tradehub_core.service;

import com.fisch_tradehub.tradehub_core.common.Constants;
import com.fisch_tradehub.tradehub_core.entity.Bill;
import com.fisch_tradehub.tradehub_core.entity.BillInfo;
import com.fisch_tradehub.tradehub_core.entity.BillStatus;
import com.fisch_tradehub.tradehub_core.entity.Cart;
import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.exception.BusinessException;
import com.fisch_tradehub.tradehub_core.exception.ResourceNotFoundException;
import com.fisch_tradehub.tradehub_core.exception.UnauthorizedAccessException;
import com.fisch_tradehub.tradehub_core.repository.BillInfoRepository;
import com.fisch_tradehub.tradehub_core.repository.BillRepository;
import com.fisch_tradehub.tradehub_core.repository.CartRepository;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.BillDTO;
import com.fisch_tradehub.tradehub_core.web.dto.BillInfoDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing bills (orders) and checkout operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillService {

  private final BillRepository billRepository;
  private final BillInfoRepository billInfoRepository;
  private final CartRepository cartRepository;
  private final UserRepository userRepository;

  /**
   * Create a bill from the user's cart items.
   *
   * @param username the username of the buyer
   * @return the created bill with items
   * @throws ResourceNotFoundException if user not found
   * @throws BusinessException if cart is empty
   */
  @Transactional
  public BillDTO checkout(String username) {
    User user = userRepository
      .findByUsername(username)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND)
      );

    List<Cart> cartItems = cartRepository.findByUser(user);
    if (cartItems.isEmpty()) {
      throw new BusinessException(Constants.CART_EMPTY);
    }

    // Calculate total
    BigDecimal total = cartItems
      .stream()
      .map(item ->
        item
          .getFish()
          .getValue()
          .multiply(BigDecimal.valueOf(item.getQuantity()))
      )
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Create Bill
    Bill bill = Bill.builder()
      .buyer(user)
      .total(total)
      .status(BillStatus.PENDING_PAYMENT)
      .build();
    bill = billRepository.save(bill);

    // Create BillInfos
    for (Cart item : cartItems) {
      BillInfo info = BillInfo.builder()
        .bill(bill)
        .fish(item.getFish())
        .price(item.getFish().getValue())
        .amount(item.getQuantity())
        .sum(
          item
            .getFish()
            .getValue()
            .multiply(BigDecimal.valueOf(item.getQuantity()))
        )
        .build();
      billInfoRepository.save(info);
    }

    // Clear Cart
    cartRepository.deleteByUserId(user.getId());

    return getBillById(bill.getId());
  }

  /**
   * Get all bills for a specific user.
   *
   * @param username the username of the buyer
   * @return list of bills
   * @throws ResourceNotFoundException if user not found
   */
  public List<BillDTO> getMyBills(String username) {
    User user = userRepository
      .findByUsername(username)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND)
      );

    return billRepository.findByBuyer(user).stream().map(this::toDto).toList();
  }

  /**
   * Get a specific bill by ID with its items.
   *
   * @param id the bill ID
   * @return the bill details
   * @throws ResourceNotFoundException if bill not found
   */
  public BillDTO getBillById(Long id) {
    Bill bill = billRepository
      .findById(id)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.BILL_NOT_FOUND)
      );
    return toDto(bill);
  }

  private BillDTO toDto(Bill bill) {
    List<BillInfo> infos = billInfoRepository.findByBillId(bill.getId());
    List<BillInfoDTO> itemDtos = infos
      .stream()
      .map(info ->
        new BillInfoDTO(
          info.getFish().getId(),
          info.getFish().getName(),
          info.getPrice(),
          info.getAmount(),
          info.getSum()
        )
      )
      .collect(Collectors.toList());

    return new BillDTO(
      bill.getId(),
      bill.getBuyer().getUsername(),
      bill.getTotal(),
      bill.getStatus(),
      bill.getCreatedAt(),
      itemDtos
    );
  }

  /**
   * Process payment for a pending bill.
   *
   * @param billId the bill ID
   * @param username the username of the requester
   * @return the updated bill
   * @throws ResourceNotFoundException if user or bill not found
   * @throws UnauthorizedAccessException if user doesn't own the bill
   * @throws BusinessException if bill is not in pending status
   */
  @Transactional
  public BillDTO payBill(Long billId, String username) {
    User user = userRepository
      .findByUsername(username)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND)
      );

    Bill bill = billRepository
      .findById(billId)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.BILL_NOT_FOUND)
      );

    if (!bill.getBuyer().getId().equals(user.getId())) {
      throw new UnauthorizedAccessException(Constants.UNAUTHORIZED_BILL_ACCESS);
    }

    if (bill.getStatus() != BillStatus.PENDING_PAYMENT) {
      throw new BusinessException(Constants.BILL_NOT_PENDING);
    }

    bill.setStatus(BillStatus.PROCESSING);
    bill = billRepository.save(bill);

    return toDto(bill);
  }

  /**
   * Cancel a pending bill (USER function).
   * Users can only cancel their own bills that are still PENDING_PAYMENT.
   *
   * @param billId the bill ID
   * @param username the username of the requester
   * @return the updated bill
   * @throws ResourceNotFoundException if user or bill not found
   * @throws UnauthorizedAccessException if user doesn't own the bill
   * @throws BusinessException if bill is not PENDING_PAYMENT
   */
  @Transactional
  public BillDTO cancelBill(Long billId, String username) {
    User user = userRepository
      .findByUsername(username)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND)
      );

    Bill bill = billRepository
      .findById(billId)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.BILL_NOT_FOUND)
      );

    if (!bill.getBuyer().getId().equals(user.getId())) {
      throw new UnauthorizedAccessException(Constants.UNAUTHORIZED_BILL_ACCESS);
    }

    if (bill.getStatus() != BillStatus.PENDING_PAYMENT) {
      throw new BusinessException(Constants.BILL_CANNOT_CANCEL);
    }

    bill.setStatus(BillStatus.CANCELLED);
    bill = billRepository.save(bill);

    return toDto(bill);
  }

  /**
   * Cancel a bill (ADMIN function).
   * Admins can cancel bills in PENDING_PAYMENT or PROCESSING status.
   * Cannot cancel COMPLETED or already CANCELLED bills.
   *
   * @param billId the bill ID
   * @return the updated bill
   * @throws ResourceNotFoundException if bill not found
   * @throws BusinessException if bill cannot be cancelled (COMPLETED or CANCELLED)
   */
  @Transactional
  public BillDTO adminCancelBill(Long billId) {
    Bill bill = billRepository
      .findById(billId)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.BILL_NOT_FOUND)
      );

    BillStatus currentStatus = bill.getStatus();

    if (!currentStatus.canTransitionTo(BillStatus.CANCELLED)) {
      throw new BusinessException(
        String.format(
          Constants.INVALID_STATUS_TRANSITION,
          currentStatus,
          BillStatus.CANCELLED
        )
      );
    }

    bill.setStatus(BillStatus.CANCELLED);
    bill = billRepository.save(bill);

    return toDto(bill);
  }

  /**
   * Get all bills in the system (admin function).
   *
   * @return list of all bills
   */
  public List<BillDTO> getAllBills() {
    return billRepository.findAll().stream().map(this::toDto).toList();
  }

  /**
   * Complete a bill (ADMIN function).
   * Marks a PROCESSING bill as COMPLETED (order delivered/fulfilled).
   *
   * @param billId the bill ID
   * @return the updated bill
   * @throws ResourceNotFoundException if bill not found
   * @throws BusinessException if bill is not in PROCESSING status
   */
  @Transactional
  public BillDTO completeBill(Long billId) {
    Bill bill = billRepository
      .findById(billId)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.BILL_NOT_FOUND)
      );

    if (bill.getStatus() != BillStatus.PROCESSING) {
      throw new BusinessException(Constants.BILL_NOT_PROCESSING);
    }

    bill.setStatus(BillStatus.COMPLETED);
    bill = billRepository.save(bill);

    return toDto(bill);
  }
}
