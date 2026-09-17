package com.arenacode.arenacode.identity;

import com.arenacode.arenacode.identity.domain.Role;
import com.arenacode.arenacode.identity.domain.User;
import com.arenacode.arenacode.identity.domain.UserStatus;
import com.arenacode.arenacode.identity.adapter.out.persistence.RoleRepository;
import com.arenacode.arenacode.identity.adapter.out.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@DataJpaTest
class IdentityDomainIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldHaveFourInitialRoles() {
        assertThat(roleRepository.count()).isEqualTo(4);
        assertThat(roleRepository.findByCode("ADMIN")).isPresent();
        assertThat(roleRepository.findByCode("MODERATOR")).isPresent();
        assertThat(roleRepository.findByCode("USER")).isPresent();
        assertThat(roleRepository.findByCode("SPECTATOR")).isPresent();
    }

    @Test
    void shouldEnforceUniqueEmailCaseInsensitive() {
        User user1 = new User("Joao", UserStatus.ACTIVE, false);
        user1.setEmail("joao@email.com");
        userRepository.save(user1);

        User user2 = new User("Joao Duplicate", UserStatus.ACTIVE, false);
        user2.setEmail("JOAO@email.com");

        assertThatThrownBy(() -> userRepository.save(user2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowMultipleGuestsWithNullEmail() {
        User guest1 = new User("Guest 1", UserStatus.GUEST, true);
        User guest2 = new User("Guest 2", UserStatus.GUEST, true);

        userRepository.save(guest1);
        userRepository.save(guest2);

        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldRejectInvalidStatus() {
        User user = new User("Test", UserStatus.ACTIVE, false);
        user.setStatus(null);

        assertThatThrownBy(() -> userRepository.save(user))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindUserByEmailIgnoreCase() {
        User user = new User("Test User", UserStatus.ACTIVE, false);
        user.setEmail("test@example.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmailIgnoreCase("TEST@EXAMPLE.COM");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }
}
