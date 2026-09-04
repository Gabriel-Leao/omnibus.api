package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RegistrationResponseTest {

  @Test
  void shouldReturnGenericResponseThatDoesNotRevealAccountExistence() {
    assertThat(RegistrationResponse.standard().message())
        .isEqualTo("If the provided email is valid, further instructions have been sent to it.");
  }
}
