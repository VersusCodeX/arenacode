package com.arenacode.arenacode.identity.application;

import com.arenacode.arenacode.identity.adapter.in.web.GuestSessionRequest;
import com.arenacode.arenacode.identity.adapter.in.web.GuestSessionResponse;
import com.arenacode.arenacode.identity.adapter.in.web.GuestUserResponse;
import com.arenacode.arenacode.identity.adapter.out.persistence.RoleRepository;
import com.arenacode.arenacode.identity.adapter.out.persistence.UserRepository;
import com.arenacode.arenacode.identity.config.JwtConfig;
import com.arenacode.arenacode.identity.domain.Role;
import com.arenacode.arenacode.identity.domain.User;
import com.arenacode.arenacode.identity.domain.UserStatus;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria sessoes de convidado (guest) para a demo publica do ArenaCode, sem exigir cadastro.
 *
 * <p><b>Nota de retencao de dados:</b> convidados sao temporarios por natureza. Esta versao nao
 * implementa exclusao automatica ou anonimizacao do registro em {@code users}; uma futura rotina
 * de limpeza deve tratar linhas com {@code is_guest = true} como candidatas a remocao/anonimizacao
 * apos um periodo de inatividade.
 */
@Service
public class GuestSessionService {

  private static final Duration GUEST_ACCESS_TOKEN_TTL = Duration.ofHours(2);
  private static final String GUEST_ROLE_CODE = "USER";
  private static final SecureRandom RANDOM = new SecureRandom();

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final JwtEncoder jwtEncoder;
  private final JwtConfig jwtConfig;

  public GuestSessionService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      JwtEncoder jwtEncoder,
      JwtConfig jwtConfig) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.jwtEncoder = jwtEncoder;
    this.jwtConfig = jwtConfig;
  }

  /**
   * Cria um usuario GUEST (status GUEST, is_guest=true, sem email/senha), atribui o papel USER e
   * emite um access token JWT de curta duracao com a claim {@code isGuest=true}.
   *
   * <p>A criacao do usuario e a atribuicao do papel ocorrem na mesma transacao: se o papel USER
   * nao estiver semeado no banco, nada e persistido.
   */
  @Transactional
  public GuestSessionResponse createGuestSession(GuestSessionRequest request) {
    Role userRole =
        roleRepository
            .findByCode(GUEST_ROLE_CODE)
            .orElseThrow(
                () -> new IllegalStateException("Role " + GUEST_ROLE_CODE + " not seeded"));

    String displayName = resolveDisplayName(request);

    User guest = new User(displayName, UserStatus.GUEST, true);
    guest.addRole(userRole);
    userRepository.save(guest);

    Set<String> roleCodes = Set.of(userRole.getCode());
    String accessToken = createGuestAccessToken(guest, roleCodes);

    GuestUserResponse userResponse =
        new GuestUserResponse(
            guest.getId(), guest.getDisplayName(), guest.getStatus(), roleCodes, guest.isGuest());
    return new GuestSessionResponse(
        accessToken, "Bearer", GUEST_ACCESS_TOKEN_TTL.toSeconds(), userResponse);
  }

  private String resolveDisplayName(GuestSessionRequest request) {
    if (request != null && request.displayName() != null && !request.displayName().isBlank()) {
      return request.displayName();
    }
    return "Guest-" + (1000 + RANDOM.nextInt(9000));
  }

  private String createGuestAccessToken(User guest, Set<String> roleCodes) {
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(jwtConfig.issuer())
            .issuedAt(now)
            .expiresAt(now.plus(GUEST_ACCESS_TOKEN_TTL))
            .subject(guest.getId().toString())
            .claim("displayName", guest.getDisplayName())
            .claim("roles", roleCodes)
            .claim("isGuest", true)
            .build();
    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
  }
}
