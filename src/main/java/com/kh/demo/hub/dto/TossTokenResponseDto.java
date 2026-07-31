package com.kh.demo.hub.dto;

public record TossTokenResponseDto(
        String access_token,
        String token_type,
        long expires_in
) {}