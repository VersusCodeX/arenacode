package com.arenacode.arenacode.identity.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import com.arenacode.arenacode.identity.application.RegistrationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints de autentica\u00e7\u00e3o e registro")
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    @Operation(
        summary = "Registrar novo usu\u00e1rio",
        description = "Cria uma nova conta de usu\u00e1rio com email e senha. Atribui automaticamente o papel USER."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usu\u00e1rio criado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = RegisteredUserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inv\u00e1lidos (email inv\u00e1lido, senha curta, displayName curto)",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email j\u00e1 cadastrado",
            content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)
            )
        )
    })
    public ResponseEntity<RegisteredUserResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        
        RegisteredUserResponse response = registrationService.register(request);
        
        java.net.URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/users/{id}")
            .buildAndExpand(response.id())
            .toUri();
        
        return ResponseEntity.created(location).body(response);
    }
}
