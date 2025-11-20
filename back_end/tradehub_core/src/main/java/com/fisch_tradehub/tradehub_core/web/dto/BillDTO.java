package com.fisch_tradehub.tradehub_core.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fisch_tradehub.tradehub_core.entity.BillStatus;

public record BillDTO(
        Long id,
        String customer,
        BigDecimal total,
        BillStatus status,
        LocalDateTime createdAt,
        List<BillInfoDTO> items) {
}
