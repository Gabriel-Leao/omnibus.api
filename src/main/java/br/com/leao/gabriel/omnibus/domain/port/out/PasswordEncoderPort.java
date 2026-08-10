package br.com.leao.gabriel.omnibus.domain.port.out;

public interface PasswordEncoderPort {

  String encode(String rawPassword);

  boolean matches(String rawPassword, String encodedPassword);
}
