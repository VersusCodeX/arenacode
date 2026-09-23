package com.arenacode.arenacode.identity.adapter.in.web;

import com.arenacode.arenacode.identity.domain.UserStatus;
import java.util.Set;
import java.util.UUID;

/** Representacao publica do usuario convidado retornada por POST /api/v1/auth/guest. */
public record GuestUserResponse(
    UUID id, String displayName, UserStatus status, Set<String> roles, boolean isGuest) {

  public GuestUserResponse {
    roles = roles == null ? Set.of() : Set.copyOf(roles);
  }
}
