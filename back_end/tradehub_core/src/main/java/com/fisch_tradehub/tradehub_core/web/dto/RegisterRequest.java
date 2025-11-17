package com.fisch_tradehub.tradehub_core.web.dto;

public record RegisterRequest(
        String username,
        String email,
        String password
) {}