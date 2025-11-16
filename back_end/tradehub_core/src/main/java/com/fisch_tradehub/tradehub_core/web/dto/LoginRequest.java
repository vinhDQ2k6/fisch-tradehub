package com.fisch_tradehub.tradehub_core.web.dto;

public record LoginRequest(
        String username,
        String password
) {
}