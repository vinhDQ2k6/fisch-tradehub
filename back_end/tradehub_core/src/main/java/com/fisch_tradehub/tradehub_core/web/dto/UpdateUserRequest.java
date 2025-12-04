package com.fisch_tradehub.tradehub_core.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for updating user role and active status.
 */
public record UpdateUserRequest(
    @NotBlank(message = "Role is required")
    String role,
    Boolean active  // nullable = no change
) {}
