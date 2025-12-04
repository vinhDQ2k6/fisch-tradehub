package com.fisch_tradehub.tradehub_core.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for password reset operation.
 */
public record ResetPasswordRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    String newPassword
) {}
