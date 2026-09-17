package com.arenacode.arenacode.identity.application;

import com.arenacode.arenacode.identity.adapter.in.web.LoginRequest;
import com.arenacode.arenacode.identity.adapter.in.web.LoginResponse;
import com.arenacode.arenacode.identity.adapter.out.persistence.UserRepository;
import com.arenacode.arenacode.identity.config.JwtProperties;
import com.arenacode.arenacode.identity.domain.User;
import com.arenacode.arenacode.identity.domain.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    @Transactional(readOnly = true)
    public LoginResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email()).orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }
        if (!canAuthenticate(user)) {
            throw new AccountDisabledException("Account is not allowed to authenticate");
        }
        Set<String> roleCodes = user.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toSet());
        String accessToken = createAccessToken(user.getId(), user.getEmail(), user.getDisplayName(), roleCodes);
        return new LoginResponse(accessToken, "Bearer", jwtProperties.getAccessTokenTtl().getSeconds(), user.getId(), user.getEmail(), user.getDisplayName(), user.getStatus(), roleCodes);
    }

    private String createAccessToken(UUID userId, String email, String displayName, Set<String> roles) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.getIssuer())
            .issuedAt(now)
            .expiresAt(now.plus(jwtProperties.getAccessTokenTtl()))
            .subject(userId.toString())
            .claim("email", email)
            .claim("displayName", displayName)
            .claim("roles", roles)
            .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private boolean canAuthenticate(User user) {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
