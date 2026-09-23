package com.arenacode.arenacode.identity.adapter.in.web;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserProfileResponse user
) {}