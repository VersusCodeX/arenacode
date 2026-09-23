package com.arenacode.arenacode.identity.adapter.in.web;

import com.arenacode.arenacode.identity.domain.ProgrammingLanguage;
import com.arenacode.arenacode.identity.domain.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record RegisteredUserResponse(
    UUID id,
    String email,
    String displayName,
    UserStatus status,
    ProgrammingLanguage preferredLanguage,
    Instant createdAt) {}
