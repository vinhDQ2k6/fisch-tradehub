package com.fisch_tradehub.tradehub_core.web.dto;

import java.math.BigDecimal;

public record BillInfoDTO(
    Long fishId,
    String fishName,
    BigDecimal price,
    Integer amount,
    BigDecimal sum
) {}
