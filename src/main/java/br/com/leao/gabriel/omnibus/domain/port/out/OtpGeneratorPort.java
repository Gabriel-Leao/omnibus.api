package br.com.leao.gabriel.omnibus.domain.port.out;

/**
 * Output port for generating and hashing one-time verification codes.
 */
public interface OtpGeneratorPort {

  /**
   * Generates a random 6-digit numeric code suitable for display to the user.
   */
  String generateCode();

  /**
   * Computes a stable, one-way hash of a code, for safe storage.
   */
  String hash(String code);
}
