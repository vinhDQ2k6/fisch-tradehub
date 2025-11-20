package com.fisch_tradehub.tradehub_core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisch_tradehub.tradehub_core.entity.Bill;
import com.fisch_tradehub.tradehub_core.entity.BillInfo;

public interface BillInfoRepository extends JpaRepository<BillInfo, Long> {

    List<BillInfo> findByBill(Bill bill);

    List<BillInfo> findByBillId(Long billId);
}