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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cobre POST /api/v1/auth/guest (sessao de convidado sem cadastro).
 *
 * <p>Nao existe, ainda, nenhum endpoint administrativo (protegido por ADMIN/MODERATOR) no MVP;
 * por isso a garantia de que o convidado nao acessa recursos administrativos e verificada aqui
 * pelas claims do token emitido (roles == ["USER"], nunca ADMIN/MODERATOR), e nao por uma chamada
 * HTTP a uma rota 403 que ainda nao existe.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class GuestSessionIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private JwtDecoder jwtDecoder;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    roleRepository.findByCode("USER").orElseGet(() -> roleRepository.save(new Role("USER", "Regular user")));
  }

  @Test
  void createGuestSessionReturns201WithGuestUser() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/guest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"displayName\": \"Visitante Teste\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(7200))
            .andExpect(jsonPath("$.user.displayName").value("Visitante Teste"))
            .andExpect(jsonPath("$.user.status").value("GUEST"))
            .andExpect(jsonPath("$.user.isGuest").value(true))
            .andExpect(jsonPath("$.user.roles[0]").value("USER"))
            .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    java.util.UUID userId = java.util.UUID.fromString(body.get("user").get("id").asText());

    Optional<User> saved = userRepository.findById(userId);
    assertThat(saved).isPresent();
    assertThat(saved.get().isGuest()).isTrue();
    assertThat(saved.get().getStatus()).isEqualTo(UserStatus.GUEST);
    assertThat(saved.get().getEmail()).isNull();
    assertThat(saved.get().getPasswordHash()).isNull();
    assertThat(saved.get().getCurrentRating()).isEqualTo(1000);
  }

  @Test
  void createGuestSessionWithoutBodyGeneratesRandomDisplayName() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/guest"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.user.displayName").value(org.hamcrest.Matchers.matchesPattern("Guest-\\d{4}")));
  }

  @Test
  void guestTokenContainsIsGuestClaimAndOnlyUserRole() throws Exception {
    MvcResult result =
        mockMvc
            .perform(post("/api/v1/auth/guest"))
            .andExpect(status().isCreated())
            .andReturn();

    String token =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .get("accessToken")
            .asText();

    Jwt jwt = jwtDecoder.decode(token);
    assertThat(jwt.getClaimAsBoolean("isGuest")).isTrue();
    assertThat(jwt.getClaimAsStringList("roles")).containsExactly("USER");
    assertThat(jwt.getClaimAsStringList("roles")).doesNotContain("ADMIN", "MODERATOR");
  }

  @Test
  void guestCanAccessMeEndpointWithIssuedToken() throws Exception {
    MvcResult result =
        mockMvc
            .perform(post("/api/v1/auth/guest"))
            .andExpect(status().isCreated())
            .andReturn();

    String token =
        objectMapper
            .readTree(result.getResponse().getContentAsString())
            .get("accessToken")
            .asText();

    mockMvc
        .perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("GUEST"));
  }

  @Test
  void createGuestSessionWithTooLongDisplayNameReturns400() throws Exception {
    String longName = "A".repeat(81);
    mockMvc
        .perform(
            post("/api/v1/auth/guest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\": \"" + longName + "\"}"))
        .andExpect(status().isBadRequest());

    assertThat(userRepository.count()).isZero();
  }

  @Test
  void guestCreationFailureDoesNotLeavePartialUser() throws Exception {
    roleRepository.deleteAll();

    mockMvc.perform(post("/api/v1/auth/guest")).andExpect(status().is5xxServerError());

    assertThat(userRepository.count()).isZero();
  }
}
