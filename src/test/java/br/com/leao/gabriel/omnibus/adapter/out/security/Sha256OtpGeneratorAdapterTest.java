package br.com.leao.gabriel.omnibus.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Sha256OtpGeneratorAdapterTest {

  private final Sha256OtpGeneratorAdapter adapter = new Sha256OtpGeneratorAdapter();

  @Test
  void shouldGenerateSixDigitNumericCode() {
    assertThat(adapter.generateCode()).matches("\\d{6}");
  }

  @Test
  void shouldGenerateExpectedSha256Hash() {
    assertThat(adapter.hash("123456"))
        .isEqualTo("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92");
  }
}
