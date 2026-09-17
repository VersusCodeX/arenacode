package com.arenacode.arenacode.identity.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arenacode.arenacode.identity.domain.Role;
import com.arenacode.arenacode.identity.domain.User;
import com.arenacode.arenacode.identity.domain.UserStatus;
import com.arenacode.arenacode.identity.adapter.out.persistence.RoleRepository;
import com.arenacode.arenacode.identity.adapter.out.persistence.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userRole = roleRepository.findByCode("USER")
                .orElseGet(() -> roleRepository.save(new Role("USER", "Regular user")));

        testUser = new User("Test User", UserStatus.ACTIVE, false);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("Senha123!"));
        testUser.setRoles(Set.of(userRole));
        userRepository.save(testUser);
    }
}
