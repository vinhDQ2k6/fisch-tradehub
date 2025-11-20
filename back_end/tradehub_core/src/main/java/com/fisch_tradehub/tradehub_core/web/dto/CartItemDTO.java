package com.fisch_tradehub.tradehub_core.web.dto;

import java.math.BigDecimal;

public record CartItemDTO(
    Long fishId,
    String fishName,
    String fishRarity,
    BigDecimal fishValue,
    BigDecimal fishWeight,
    Integer quantity
) {}
