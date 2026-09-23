package com.arenacode.arenacode.identity.application;

import com.arenacode.arenacode.identity.adapter.in.web.LoginRequest;
import com.arenacode.arenacode.identity.adapter.in.web.LoginResponse;
import com.arenacode.arenacode.identity.adapter.in.web.UserProfileResponse;
import com.arenacode.arenacode.identity.adapter.out.persistence.UserRepository;
import com.arenacode.arenacode.identity.config.JwtConfig;
import com.arenacode.arenacode.identity.domain.User;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
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
  private final JwtConfig jwtConfig;

  public AuthenticationService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtEncoder jwtEncoder,
      JwtConfig jwtConfig) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtEncoder = jwtEncoder;
    this.jwtConfig = jwtConfig;
  }

  @Transactional(readOnly = true)
  public LoginResponse authenticate(LoginRequest request) {
    User user =
        userRepository
            .findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new AuthenticationException("Invalid credentials");
    }
    if (!canAuthenticate(user)) {
      throw new AccountDisabledException("Account is not allowed to authenticate");
    }
    Set<String> roleCodes =
        user.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toSet());
    String accessToken = createAccessToken(user.getId(), user.getDisplayName(), roleCodes);
    long ttlSeconds = jwtConfig.accessTokenTtl().toSeconds();
    return new LoginResponse(accessToken, "Bearer", ttlSeconds, toProfile(user, roleCodes));
  }

  @Transactional(readOnly = true)
  public UserProfileResponse currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      throw new AuthenticationException("Authentication required");
    }

    UUID userId;
    try {
      userId = UUID.fromString(authentication.getName());
    } catch (IllegalArgumentException ex) {
      throw new AuthenticationException("Invalid user identity");
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new AuthenticationException("User not found"));

    Set<String> roleCodes =
        user.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toSet());
    return toProfile(user, roleCodes);
  }

  private UserProfileResponse toProfile(User user, Set<String> roleCodes) {
    return new UserProfileResponse(
        user.getId(), user.getEmail(), user.getDisplayName(), user.getStatus(), roleCodes);
  }

  private String createAccessToken(UUID userId, String displayName, Set<String> roles) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(jwtConfig.issuer())
            .issuedAt(now)
            .expiresAt(now.plus(jwtConfig.accessTokenTtl()))
            .subject(userId.toString())
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
