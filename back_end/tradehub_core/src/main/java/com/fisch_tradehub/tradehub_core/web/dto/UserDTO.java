package com.fisch_tradehub.tradehub_core.web.dto;

public record UserDTO(
        Long id,
        String username,
        String email,
        String role
) {
}