package com.arenacode.arenacode.identity.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) jwt.getClaimAsMap("roles").keySet();
        Collection<GrantedAuthority> authorities;
        if (roles == null || roles.isEmpty()) {
            authorities = new ArrayList<>();
        } else {
            authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
        }
        String subject = jwt.getSubject();
        return new JwtAuthenticationToken(jwt, authorities, subject);
    }
}
