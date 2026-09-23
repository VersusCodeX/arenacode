package com.arenacode.arenacode.identity.adapter.in.web;

import jakarta.validation.constraints.Size;

/**
 * Corpo opcional do POST /api/v1/auth/guest. Se omitido (ou {@code displayName} ausente/em
 * branco), um nome aleatorio no formato {@code Guest-XXXX} e gerado pelo servidor.
 */
public record GuestSessionRequest(
    @Size(max = 80, message = "displayName must be at most 80 characters") String displayName) {}
