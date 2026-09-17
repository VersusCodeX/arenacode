package com.arenacode.arenacode.identity.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
    @Email
    @Size(max = 320)
    String email,

    @NotBlank
    @Size(min = 12, max = 128)
    String password,

    @NotBlank
    @Size(min = 3, max = 80)
    String displayName
) {}
