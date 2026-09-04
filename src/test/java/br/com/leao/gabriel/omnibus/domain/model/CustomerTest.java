package br.com.leao.gabriel.omnibus.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CustomerTest {

  @Test
  void shouldCreatePendingActivationCustomer() {
    var customer =
        Customer.register("Maria", "maria@example.com", "hash", LocalDate.of(1990, 1, 1), null);

    assertThat(customer.getStatus()).isEqualTo(UserStatus.PENDING_ACTIVATION);
    assertThat(customer.canUseOtp(OtpType.ACCOUNT_ACTIVATION)).isTrue();
    assertThat(customer.canUseOtp(OtpType.PASSWORD_RESET)).isFalse();
  }

  @Test
  void shouldActivateAndAllowPasswordReset() {
    var customer =
        Customer.register("Maria", "maria@example.com", "hash", LocalDate.of(1990, 1, 1), null)
            .activate();

    assertThat(customer.isActivated()).isTrue();
    assertThat(customer.canUseOtp(OtpType.PASSWORD_RESET)).isTrue();
  }

  @Test
  void shouldRejectFutureBirthDate() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Customer.register(
                "Maria", "maria@example.com", "hash", LocalDate.now().plusDays(1), null));
  }
}
