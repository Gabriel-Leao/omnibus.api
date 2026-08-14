package br.com.leao.gabriel.omnibus.domain.port.out;

/**
 * Output port for password encoding and verification.
 */
public interface PasswordEncoderPort {

  /**
   * Encodes a raw password.
   */
  String encode(String rawPassword);

  /**
   * Checks whether a raw password matches an encoded password.
   */
  boolean matches(String rawPassword, String encodedPassword);
}
