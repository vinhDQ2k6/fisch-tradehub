package com.fisch_tradehub.tradehub_core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisch_tradehub.tradehub_core.entity.Bill;
import com.fisch_tradehub.tradehub_core.entity.BillStatus;
import com.fisch_tradehub.tradehub_core.entity.User;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByBuyer(User buyer);

    List<Bill> findByBuyerId(Long buyerId);

    List<Bill> findBySeller(User seller);

    List<Bill> findBySellerId(Long sellerId);

    List<Bill> findByStatus(BillStatus status);
}