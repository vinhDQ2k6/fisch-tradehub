package com.fisch_tradehub.tradehub_core.web.dto;

import java.math.BigDecimal;

public record FishDTO(
        Long id,
        String name,
        String rarity,
        BigDecimal value,
        BigDecimal weight
) {}
