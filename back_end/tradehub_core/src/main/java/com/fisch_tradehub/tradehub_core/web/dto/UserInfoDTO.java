package com.fisch_tradehub.tradehub_core.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UserInfoDTO(
    @Size(max = 100, message = "Fullname must be at most 100 characters")
    String fullname,

    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age must be at most 150")
    Short age,

    Boolean gender
) {}
