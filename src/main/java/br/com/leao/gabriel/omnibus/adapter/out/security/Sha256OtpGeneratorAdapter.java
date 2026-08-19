package br.com.leao.gabriel.omnibus.adapter.out.security;

import br.com.leao.gabriel.omnibus.domain.port.out.OtpGeneratorPort;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Generates 6-digit numeric OTP codes and hashes them using SHA-256.
 */
@Component
public class Sha256OtpGeneratorAdapter implements OtpGeneratorPort {

  private static final int CODE_LENGTH = 6;
  private final SecureRandom random = new SecureRandom();

  @Override
  public String generateCode() {
    int bound = (int) Math.pow(10, CODE_LENGTH);
    int code = random.nextInt(bound);
    return String.format("%0" + CODE_LENGTH + "d", code);
  }

  @Override
  public String hash(String code) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(code.getBytes());
      StringBuilder hex = new StringBuilder();
      for (byte b : hashBytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }
}