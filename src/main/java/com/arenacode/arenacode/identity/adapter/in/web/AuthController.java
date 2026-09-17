package com.arenacode.arenacode.identity.adapter.in.web;

import com.arenacode.arenacode.identity.application.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints de autenticação e login")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar com e-mail e senha", description = "Retorna access token JWT Bearer.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Payload inválido", content = @Content(mediaType = "application/problem+json")),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content(mediaType = "application/problem+json")),
        @ApiResponse(responseCode = "403", description = "Conta desabilitada", content = @Content(mediaType = "application/problem+json"))
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody @Parameter(description = "Credenciais") LoginRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Obter perfil do usuário autenticado", description = "Retorna dados públicos do usuário autenticado via JWT.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Perfil do usuário", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(mediaType = "application/problem+json"))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserProfileResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        String subject = jwt.getSubject();
        UUID userId = UUID.fromString(subject);
        String email = jwt.getClaimAsString("email");
        String displayName = jwt.getClaimAsString("displayName");
        @SuppressWarnings("unchecked")
        java.util.List<String> rolesList = jwt.getClaimAsStringList("roles");
        java.util.Set<String> roles = rolesList != null ? java.util.Set.copyOf(rolesList) : java.util.Set.of();
        return ResponseEntity.ok(new UserProfileResponse(userId, email, displayName, null, roles));
    }
}
