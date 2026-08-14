package br.com.leao.gabriel.omnibus.adapter.out.security;

import br.com.leao.gabriel.omnibus.domain.port.out.PasswordEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adapts BCrypt password encoding to the application's password encoder port.
 */
@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort {

  private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

  /**
   * Encodes a raw password using BCrypt.
   */
  @Override
  public String encode(String rawPassword) {
    return delegate.encode(rawPassword);
  }

  /**
   * Checks whether a raw password matches an encoded password.
   */
  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    return delegate.matches(rawPassword, encodedPassword);
  }
}
