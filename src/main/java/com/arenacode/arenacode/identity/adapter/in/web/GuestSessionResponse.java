package com.arenacode.arenacode.identity.adapter.in.web;

public record GuestSessionResponse(
    String accessToken, String tokenType, long expiresIn, GuestUserResponse user) {}
