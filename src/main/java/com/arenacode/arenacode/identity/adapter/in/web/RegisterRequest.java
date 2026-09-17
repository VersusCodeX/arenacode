package com.arenacode.arenacode.identity.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 320, message = "email must be at most 320 characters")
    String email,

    @NotBlank(message = "password is required")
    @Size(min = 12, message = "password must be at least 12 characters")
    @Size(max = 128, message = "password must be at most 128 characters")
    String password,

    @NotBlank(message = "displayName is required")
    @Size(min = 3, message = "displayName must be at least 3 characters")
    @Size(max = 80, message = "displayName must be at most 80 characters")
    String displayName
) {}
