package br.com.leao.gabriel.omnibus.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BcryptPasswordEncoderAdapterTest {

  private final BcryptPasswordEncoderAdapter adapter = new BcryptPasswordEncoderAdapter();

  @Test
  void shouldEncodeAndMatchPassword() {
    String encoded = adapter.encode("Senha@123");

    assertThat(encoded).isNotEqualTo("Senha@123");
    assertThat(adapter.matches("Senha@123", encoded)).isTrue();
  }

  @Test
  void shouldNotMatchDifferentPassword() {
    assertThat(adapter.matches("OutraSenha@123", adapter.encode("Senha@123"))).isFalse();
  }
}
