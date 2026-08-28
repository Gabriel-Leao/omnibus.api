package br.com.leao.gabriel.omnibus.adapter.out.security;

import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Parses and validates JWT tokens issued by {@link JwtTokenIssuerAdapter}.
 */
@Component
public class JwtTokenParser {

  private final SecretKey signingKey;

  /**
   * Creates a JWT token parser using the configured signing secret.
   */
  public JwtTokenParser(@Value("${jwt.secret}") String secret) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Parses the token and returns its claims.
   *
   * @param token the JWT to parse
   *
   * @throws JwtException if the token is invalid, malformed, or expired
   */
  public Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Extracts the token purpose from the given claims.
   *
   * @param claims the JWT claims
   *
   * @return the purpose for which the token was issued
   *
   * @throws IllegalArgumentException if the purpose is missing or unknown
   */
  public OtpType extractPurpose(Claims claims) {
    String purpose = claims.get("purpose", String.class);
    return OtpType.valueOf(purpose);
  }

  /**
   * Extracts the authorities granted by the token from the given claims.
   *
   * @param claims the JWT claims
   *
   * @return the authorities granted by the token, or an empty list if none are present
   */
  public List<String> extractAuthorities(Claims claims) {
    List<?> authorities = claims.get("authorities", List.class);

    if (authorities == null) {
      return List.of();
    }

    return authorities.stream().map(Object::toString).toList();
  }
}
