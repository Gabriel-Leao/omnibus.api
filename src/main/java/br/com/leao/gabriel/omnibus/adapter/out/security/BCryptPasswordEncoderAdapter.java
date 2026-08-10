package br.com.leao.gabriel.omnibus.adapter.out.security;

import br.com.leao.gabriel.omnibus.domain.port.out.PasswordEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

  private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

  @Override
  public String encode(String rawPassword) {
    return delegate.encode(rawPassword);
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    return delegate.matches(rawPassword, encodedPassword);
  }
}