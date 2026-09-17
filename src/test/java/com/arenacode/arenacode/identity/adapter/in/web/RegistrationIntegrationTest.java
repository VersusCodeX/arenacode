package com.arenacode.arenacode.identity.adapter.in.web;

import com.arenacode.arenacode.identity.adapter.out.persistence.RoleRepository;
import com.arenacode.arenacode.identity.adapter.out.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldRegisterUserAndReturn201() throws Exception {
        String requestBody = """
            {
                "email": "newuser@example.com",
                "password": "securepassword123",
                "displayName": "New User"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.displayName").value("New User"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.preferredLanguage").value("JAVA_21"))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldReturn409ForDuplicateEmail() throws Exception {
        // First registration
        String requestBody = """
            {
                "email": "duplicate@example.com",
                "password": "securepassword123",
                "displayName": "First User"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());

        // Second registration with same email
        String requestBody2 = """
            {
                "email": "duplicate@example.com",
                "password": "anotherpassword123",
                "displayName": "Second User"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400ForInvalidEmail() throws Exception {
        String requestBody = """
            {
                "email": "invalid-email",
                "password": "securepassword123",
                "displayName": "Test User"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForShortPassword() throws Exception {
        String requestBody = """
            {
                "email": "test@example.com",
                "password": "short",
                "displayName": "Test User"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForShortDisplayName() throws Exception {
        String requestBody = """
            {
                "email": "test@example.com",
                "password": "securepassword123",
                "displayName": "AB"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }
}
