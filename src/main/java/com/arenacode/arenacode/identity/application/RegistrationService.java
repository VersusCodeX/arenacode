package com.arenacode.arenacode.identity.application;

import com.arenacode.arenacode.identity.adapter.in.web.RegisterRequest;
import com.arenacode.arenacode.identity.adapter.in.web.RegisteredUserResponse;
import com.arenacode.arenacode.identity.adapter.out.persistence.RoleRepository;
import com.arenacode.arenacode.identity.adapter.out.persistence.UserRepository;
import com.arenacode.arenacode.identity.domain.Role;
import com.arenacode.arenacode.identity.domain.User;
import com.arenacode.arenacode.identity.domain.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public RegistrationService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public RegisteredUserResponse register(RegisterRequest request) {
    if (userRepository.existsByEmailIgnoreCase(request.email())) {
      throw new EmailAlreadyExistsException(request.email());
    }

    User user = new User(request.displayName(), UserStatus.ACTIVE, false);
    user.setEmail(request.email().toLowerCase());
    user.setPasswordHash(passwordEncoder.encode(request.password()));

    Role userRole =
        roleRepository
            .findByCode("USER")
            .orElseThrow(() -> new IllegalStateException("Role USER not found"));
    user.addRole(userRole);

    User savedUser = userRepository.save(user);

    return new RegisteredUserResponse(
        savedUser.getId(),
        savedUser.getEmail(),
        savedUser.getDisplayName(),
        savedUser.getStatus(),
        savedUser.getPreferredLanguage(),
        savedUser.getCreatedAt());
  }
}
