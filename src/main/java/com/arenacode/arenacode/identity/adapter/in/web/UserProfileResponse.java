package com.arenacode.arenacode.identity.adapter.in.web;

import com.arenacode.arenacode.identity.domain.UserStatus;
import java.util.Set;
import java.util.UUID;

public record UserProfileResponse(
    UUID id, String email, String displayName, UserStatus status, Set<String> roles) {}
