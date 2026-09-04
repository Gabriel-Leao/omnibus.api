package br.com.leao.gabriel.omnibus.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.leao.gabriel.omnibus.domain.model.UserStatus;
import org.junit.jupiter.api.Test;

class UserPrincipalTest {

  @Test
  void shouldCreateEnabledCustomerPrincipal() {
    var principal = UserPrincipal.ofCustomer("id", "user@example.com", "hash", UserStatus.ACTIVE);

    assertThat(principal.getUsername()).isEqualTo("user@example.com");
    assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_CUSTOMER");
    assertThat(principal.isEnabled()).isTrue();
  }

  @Test
  void shouldLockSuspendedAndBannedAccounts() {
    assertThat(
            UserPrincipal.ofStaff("id", "a@b.com", "hash", UserStatus.SUSPENDED, "EDITOR")
                .isAccountNonLocked())
        .isFalse();
    assertThat(
            UserPrincipal.ofStaff("id", "a@b.com", "hash", UserStatus.BANNED, "EDITOR")
                .isAccountNonLocked())
        .isFalse();
  }
}
