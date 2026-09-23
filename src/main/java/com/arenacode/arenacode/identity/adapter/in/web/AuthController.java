package com.arenacode.arenacode.identity.adapter.in.web;

import com.arenacode.arenacode.identity.application.AuthenticationService;
import com.arenacode.arenacode.identity.application.GuestSessionService;
import com.arenacode.arenacode.identity.application.RegistrationService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private final AuthenticationService authenticationService;
  private final RegistrationService registrationService;
  private final GuestSessionService guestSessionService;

  public AuthController(
      AuthenticationService authenticationService,
      RegistrationService registrationService,
      GuestSessionService guestSessionService) {
    this.authenticationService = authenticationService;
    this.registrationService = registrationService;
    this.guestSessionService = guestSessionService;
  }

  @PostMapping("/api/v1/auth/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authenticationService.authenticate(request));
  }

  @PostMapping("/api/v1/auth/register")
  public ResponseEntity<RegisteredUserResponse> register(
      @Valid @RequestBody RegisterRequest request) {
    RegisteredUserResponse response = registrationService.register(request);
    return ResponseEntity.created(URI.create("/api/v1/users/" + response.id())).body(response);
  }

  /**
   * Cria uma sessao de convidado (guest) para demo publica, sem exigir cadastro. Retorna 201
   * porque um novo usuario GUEST e efetivamente criado em {@code users}.
   */
  @PostMapping("/api/v1/auth/guest")
  public ResponseEntity<GuestSessionResponse> createGuestSession(
      @Valid @RequestBody(required = false) GuestSessionRequest request) {
    GuestSessionResponse response = guestSessionService.createGuestSession(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/api/v1/me")
  public ResponseEntity<UserProfileResponse> me() {
    return ResponseEntity.ok(authenticationService.currentUser());
  }
}
