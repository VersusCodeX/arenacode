package com.arenacode.arenacode.identity.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserDomainTest {

  @Test
  void isActiveShouldReturnTrueForActiveStatus() {
    User user = new User("Active User", UserStatus.ACTIVE, false);
    assertThat(user.isActive()).isTrue();
  }

  @Test
  void isActiveShouldReturnFalseForOtherStatuses() {
    assertThat(new User("Guest", UserStatus.GUEST, true).isActive()).isFalse();
    assertThat(new User("Suspended", UserStatus.SUSPENDED, false).isActive()).isFalse();
    assertThat(new User("Banned", UserStatus.BANNED, false).isActive()).isFalse();
    assertThat(new User("Deleted", UserStatus.DELETED, false).isActive()).isFalse();
  }

  @Test
  void isGuestShouldReturnTrueForGuest() {
    User guest = new User("Guest User", UserStatus.GUEST, true);
    assertThat(guest.isGuest()).isTrue();
  }

  @Test
  void isGuestShouldReturnFalseForNonGuest() {
    User user = new User("Regular User", UserStatus.ACTIVE, false);
    assertThat(user.isGuest()).isFalse();
  }

  @Test
  void canAuthenticateShouldReturnTrueForActiveNonGuestWithEmail() {
    User user = new User("Auth User", UserStatus.ACTIVE, false);
    user.setEmail("user@example.com");
    assertThat(user.canAuthenticate()).isTrue();
  }

  @Test
  void canAuthenticateShouldReturnFalseForGuest() {
    User guest = new User("Guest", UserStatus.GUEST, true);
    assertThat(guest.canAuthenticate()).isFalse();
  }

  @Test
  void canAuthenticateShouldReturnFalseForSuspended() {
    User user = new User("Suspended", UserStatus.SUSPENDED, false);
    user.setEmail("suspended@example.com");
    assertThat(user.canAuthenticate()).isFalse();
  }

  @Test
  void hasRoleShouldReturnTrueWhenRoleExists() {
    User user = new User("Role User", UserStatus.ACTIVE, false);
    Role adminRole = new Role("ADMIN", "Administrator");
    user.getRoles().add(adminRole);

    assertThat(user.hasRole("ADMIN")).isTrue();
    assertThat(user.hasRole("admin")).isTrue();
  }

  @Test
  void hasRoleShouldReturnFalseWhenRoleDoesNotExist() {
    User user = new User("No Role User", UserStatus.ACTIVE, false);
    assertThat(user.hasRole("ADMIN")).isFalse();
  }
}
