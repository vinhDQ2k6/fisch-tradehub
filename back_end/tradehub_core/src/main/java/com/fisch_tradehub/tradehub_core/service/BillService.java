package com.fisch_tradehub.tradehub_core.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisch_tradehub.tradehub_core.entity.Bill;
import com.fisch_tradehub.tradehub_core.entity.BillInfo;
import com.fisch_tradehub.tradehub_core.entity.BillStatus;
import com.fisch_tradehub.tradehub_core.entity.Cart;
import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.repository.BillInfoRepository;
import com.fisch_tradehub.tradehub_core.repository.BillRepository;
import com.fisch_tradehub.tradehub_core.repository.CartRepository;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.BillDTO;
import com.fisch_tradehub.tradehub_core.web.dto.BillInfoDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillService {

    private final BillRepository billRepository;
    private final BillInfoRepository billInfoRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Transactional
    public BillDTO checkout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Cart> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Calculate total
        BigDecimal total = cartItems.stream()
                .map(item -> item.getFish().getValue().multiply(BigDecimal.valueOf(item.getQuantity())))
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
                    .sum(item.getFish().getValue().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
            billInfoRepository.save(info);
        }

        // Clear Cart
        cartRepository.deleteByUserId(user.getId());

        return getBillById(bill.getId());
    }

    public List<BillDTO> getMyBills(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return billRepository.findByBuyer(user).stream()
                .map(this::toDto)
                .toList();
    }

    public BillDTO getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        return toDto(bill);
    }

    private BillDTO toDto(Bill bill) {
        List<BillInfo> infos = billInfoRepository.findByBillId(bill.getId());
        List<BillInfoDTO> itemDtos = infos.stream()
                .map(info -> new BillInfoDTO(
                        info.getFish().getId(),
                        info.getFish().getName(),
                        info.getPrice(),
                        info.getAmount(),
                        info.getSum()
                ))
                .collect(Collectors.toList());

        return new BillDTO(
                bill.getId(),
                bill.getTotal(),
                bill.getStatus(),
                bill.getCreatedAt(),
                itemDtos
        );
    }
}
