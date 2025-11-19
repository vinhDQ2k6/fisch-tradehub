package com.fisch_tradehub.tradehub_core.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FishRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Rarity is required") String rarity,
        @NotNull(message = "Value is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Value must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Value must have up to 8 digits and 2 decimals") BigDecimal value,
        @NotNull(message = "Weight is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Weight must have up to 8 digits and 2 decimals") BigDecimal weight
) {}
