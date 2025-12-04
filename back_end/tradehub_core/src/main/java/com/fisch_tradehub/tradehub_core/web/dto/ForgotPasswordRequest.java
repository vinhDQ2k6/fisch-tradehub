package com.fisch_tradehub.tradehub_core.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for forgot password operation.
 */
public record ForgotPasswordRequest(
    @NotBlank @Email String email
) {}
