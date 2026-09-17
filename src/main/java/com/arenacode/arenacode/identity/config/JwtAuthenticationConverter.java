package com.arenacode.arenacode.identity.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationConverter extends JwtAuthenticationConverter {

    @Override
    protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) jwt.getClaimAsMap("roles").keySet();
        if (roles == null) {
            return new ArrayList<>();
        }
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());
    }
}
