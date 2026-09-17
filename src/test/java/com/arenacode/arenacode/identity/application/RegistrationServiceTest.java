package com.arenacode.arenacode.identity.application;

import com.arenacode.arenacode.identity.adapter.in.web.RegisterRequest;
import com.arenacode.arenacode.identity.adapter.out.persistence.RoleRepository;
import com.arenacode.arenacode.identity.adapter.out.persistence.UserRepository;
import com.arenacode.arenacode.identity.domain.Role;
import com.arenacode.arenacode.identity.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void shouldRegisterUserWithValidData() {
        // Given
        RegisterRequest request = new RegisterRequest("test@example.com", "password123456", "Test User");
        Role userRole = new Role("USER", "Regular user");
        userRole.setId(UUID.randomUUID());
        
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(roleRepository.findByCode("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedHash");
        
        User savedUser = new User(request.displayName(), null, false);
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(request.email().toLowerCase());
        savedUser.setPasswordHash("$2a$10$encodedHash");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        var response = registrationService.register(request);

        // Then
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.displayName()).isEqualTo("Test User");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    void shouldThrowEmailAlreadyExistsException() {
        // Given
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123456", "Test User");
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> registrationService.register(request))
            .isInstanceOf(EmailAlreadyExistsException.class);
    }
}
