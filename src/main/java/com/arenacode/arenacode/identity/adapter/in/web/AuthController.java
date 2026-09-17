package com.arenacode.arenacode.identity.adapter.in.web;

import com.arenacode.arenacode.identity.application.EmailAlreadyExistsException;
import com.arenacode.arenacode.identity.application.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(RegistrationService registrationService,
                          PasswordEncoder passwordEncoder) {
        this.registrationService = registrationService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisteredUserResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        
        RegisteredUserResponse response = registrationService.register(request);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/users/{id}")
            .buildAndExpand(response.id())
            .toUri();
        
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/test-password")
    public ResponseEntity<Boolean> testPassword(@RequestBody String password) {
        // Endpoint temporÃ¡rio para teste - remover em produção
        return ResponseEntity.ok(passwordEncoder.matches(password, "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi"));
    }
}
