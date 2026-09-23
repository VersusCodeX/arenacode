package com.arenacode.arenacode.identity.adapter.in.web;

import com.arenacode.arenacode.identity.application.AuthenticationService;
import com.arenacode.arenacode.identity.application.RegistrationService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private final AuthenticationService authenticationService;
  private final RegistrationService registrationService;

  public AuthController(
      AuthenticationService authenticationService, RegistrationService registrationService) {
    this.authenticationService = authenticationService;
    this.registrationService = registrationService;
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

  @GetMapping("/api/v1/me")
  public ResponseEntity<UserProfileResponse> me() {
    return ResponseEntity.ok(authenticationService.currentUser());
  }
}
