package com.fisch_tradehub.tradehub_core.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisch_tradehub.tradehub_core.web.model.Bill;
import com.fisch_tradehub.tradehub_core.web.model.BillInfo;

public interface BillInfoRepository extends JpaRepository<BillInfo, Long> {

    List<BillInfo> findByBill(Bill bill);

    List<BillInfo> findByBillId(Long billId);
}