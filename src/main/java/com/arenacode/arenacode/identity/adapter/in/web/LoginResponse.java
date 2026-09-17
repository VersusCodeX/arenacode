package com.arenacode.arenacode.identity.adapter.in.web;

import com.arenacode.arenacode.identity.domain.UserStatus;
import java.util.Set;
import java.util.UUID;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UUID id,
    String email,
    String displayName,
    UserStatus status,
    Set<String> roles
) {}
