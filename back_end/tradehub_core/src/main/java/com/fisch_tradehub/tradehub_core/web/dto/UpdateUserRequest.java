package com.fisch_tradehub.tradehub_core.web.dto;

/**
 * Request DTO for updating user details.
 * Both fields are optional to allow partial updates.
 */
public record UpdateUserRequest(
    String role,  // nullable = no change to role
    Boolean active  // nullable = no change to active status
) {}
