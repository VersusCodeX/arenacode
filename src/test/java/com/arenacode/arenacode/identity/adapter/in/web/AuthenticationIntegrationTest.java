package com.arenacode.arenacode.identity.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arenacode.arenacode.identity.adapter.out.persistence.RoleRepository;
import com.arenacode.arenacode.identity.adapter.out.persistence.UserRepository;
import com.arenacode.arenacode.identity.domain.Role;
import com.arenacode.arenacode.identity.domain.User;
import com.arenacode.arenacode.identity.domain.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

  private static final String RAW_PASSWORD = "Senha123!";

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private JwtEncoder jwtEncoder;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private User testUser;
  private Role userRole;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    userRole =
        roleRepository
            .findByCode("USER")
            .orElseGet(() -> roleRepository.save(new Role("USER", "Regular user")));

    testUser = new User("Test User", UserStatus.ACTIVE, false);
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash(passwordEncoder.encode(RAW_PASSWORD));
    testUser.setRoles(Set.of(userRole));
    userRepository.save(testUser);
  }

  // ---------- POST /api/v1/auth/login ----------

  @Test
  void loginWithValidCredentialsReturnsAccessToken() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload("test@example.com", RAW_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresIn").value(900))
        .andExpect(jsonPath("$.user.email").value("test@example.com"))
        .andExpect(jsonPath("$.user.roles[0]").value("USER"));
  }

  @Test
  void issuedTokenIsValidAndUsableOnProtectedRoute() throws Exception {
    String token = login("test@example.com", RAW_PASSWORD);

    mockMvc
        .perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("test@example.com"));
  }

  @Test
  void accessTokenDoesNotContainPasswordOrHash() throws Exception {
    String token = login("test@example.com", RAW_PASSWORD);

    String[] parts = token.split("\\.");
    String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

    assertThat(payload).doesNotContainIgnoringCase("password");
    assertThat(payload).doesNotContainIgnoringCase(RAW_PASSWORD);
    assertThat(payload).doesNotContain(testUser.getPasswordHash());
  }

  @Test
  void loginWithInvalidPasswordReturns401Generic() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload("test@example.com", "wrong-password")))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Invalid credentials"));
  }

  @Test
  void loginWithNonExistentEmailReturnsSameGeneric401AsWrongPassword() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload("does-not-exist@example.com", RAW_PASSWORD)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Invalid credentials"));
  }

  @Test
  void loginWithInvalidPayloadReturns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void bannedUserCannotAuthenticate() throws Exception {
    testUser.setStatus(UserStatus.BANNED);
    userRepository.save(testUser);

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload("test@example.com", RAW_PASSWORD)))
        .andExpect(status().isForbidden());
  }

  @Test
  void suspendedUserCannotAuthenticate() throws Exception {
    testUser.setStatus(UserStatus.SUSPENDED);
    userRepository.save(testUser);

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload("test@example.com", RAW_PASSWORD)))
        .andExpect(status().isForbidden());
  }

  @Test
  void deletedUserCannotAuthenticate() throws Exception {
    testUser.setStatus(UserStatus.DELETED);
    userRepository.save(testUser);

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload("test@example.com", RAW_PASSWORD)))
        .andExpect(status().isForbidden());
  }

  // ---------- GET /api/v1/me ----------

  @Test
  void meWithoutTokenReturns401() throws Exception {
    mockMvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void meWithValidTokenReturnsUserProfile() throws Exception {
    String token = login("test@example.com", RAW_PASSWORD);

    mockMvc
        .perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.displayName").value("Test User"))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.roles[0]").value("USER"));
  }

  @Test
  void meWithMalformedTokenReturns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/me").header("Authorization", "Bearer not-a-real-jwt"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void meWithExpiredTokenReturns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/me").header("Authorization", "Bearer " + expiredTokenFor(testUser)))
        .andExpect(status().isUnauthorized());
  }

  // ---------- helpers ----------

  private String loginPayload(String email, String password) throws Exception {
    return objectMapper.writeValueAsString(new LoginRequest(email, password));
  }

  private String login(String email, String password) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginPayload(email, password)))
            .andExpect(status().isOk())
            .andReturn();
    LoginResponse response =
        objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
    return response.accessToken();
  }

  private String expiredTokenFor(User user) {
    Instant issuedAt = Instant.now().minus(Duration.ofMinutes(30));
    Instant expiredAt = Instant.now().minus(Duration.ofMinutes(15));
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("arenacode-test")
            .issuedAt(issuedAt)
            .expiresAt(expiredAt)
            .subject(user.getId().toString())
            .claim("displayName", user.getDisplayName())
            .claim("roles", Set.of("USER"))
            .build();
    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
  }
}
