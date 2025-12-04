package com.fisch_tradehub.tradehub_core.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
    @NotBlank String role,
    Boolean active  // nullable = no change
) {}
