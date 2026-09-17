package com.arenacode.arenacode.identity.application;

import com.arenacode.arenacode.identity.adapter.in.web.LoginRequest;
import com.arenacode.arenacode.identity.adapter.in.web.LoginResponse;
import com.arenacode.arenacode.identity.adapter.in.web.UserProfileResponse;
import com.arenacode.arenacode.identity.adapter.out.persistence.UserRepository;
import com.arenacode.arenacode.identity.domain.User;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final Environment env;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder, Environment env) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.env = env;
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
        long ttlSeconds = Long.parseLong(env.getProperty("app.jwt.access-token-ttl", "15m").replace("m", "")) * 60;
        return new LoginResponse(accessToken, "Bearer", ttlSeconds, user.getId(), user.getEmail(), user.getDisplayName(), user.getStatus(), roleCodes);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationException("Authentication required");
        }

        UUID userId;
        try {
            userId = UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new AuthenticationException("Invalid user identity");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        Set<String> roleCodes = user.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toSet());


        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus(),
                roleCodes);
    }

    private String createAccessToken(UUID userId, String email, String displayName, Set<String> roles) {
        Instant now = Instant.now();
        Duration ttl = Duration.ofMinutes(15);
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(env.getProperty("APP_JWT_ISSUER", "arenacode.dev"))
            .issuedAt(now)
            .expiresAt(now.plus(ttl))
            .subject(userId.toString())
            .claim("email", email)
            .claim("displayName", displayName)
            .claim("roles", roles)
            .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private boolean canAuthenticate(User user) {
        return user.getStatus() == com.arenacode.arenacode.identity.domain.UserStatus.ACTIVE;
    }
}
