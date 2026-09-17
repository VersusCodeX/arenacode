package com.arenacode.arenacode.identity.adapter.in.web;

import com.arenacode.arenacode.identity.application.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints de autenticação e registro")
public class AuthController {

  private final RegistrationService registrationService;

  public AuthController(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @PostMapping("/register")
  @Operation(
      summary = "Registrar usuário com e-mail e senha",
      description = "Cria uma nova conta permanente com papel USER. A senha é armazenada apenas como hash BCrypt.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Usuário criado com sucesso",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RegisteredUserResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Payload inválido (validação falhou)",
        content = @Content(mediaType = "application/problem+json")),
    @ApiResponse(
        responseCode = "409",
        description = "E-mail já cadastrado",
        content = @Content(mediaType = "application/problem+json"))
  })
  public ResponseEntity<RegisteredUserResponse> register(
      @Valid @RequestBody @Parameter(description = "Dados de registro") RegisterRequest request) {
    RegisteredUserResponse response = registrationService.register(request);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/../../users/{id}")
            .buildAndExpand(response.id())
            .toUri();

    return ResponseEntity.created(location).body(response);
  }
}
