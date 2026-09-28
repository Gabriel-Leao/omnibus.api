package br.com.leao.gabriel.omnibus.kernel.adapter.out.security;

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
 * Parses and validates JWT tokens issued by the identity module's token issuer.
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
   * @throws JwtException if the token is invalid, malformed, or expired
   */
  public Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }

  /**
   * Extracts the raw token purpose claim, if present.
   *
   * <p>Kernel deliberately treats this as an opaque string — it has no knowledge of what
   * purposes exist or what they mean. Callers that need semantic meaning (e.g. mapping the value to
   * a domain-specific enum) must do that translation themselves.
   *
   * @param claims the JWT claims
   * @return the raw purpose claim, or {@code null} if the token carries none
   */
  public String extractPurpose(Claims claims) {
    return claims.get("purpose", String.class);
  }

  /**
   * Extracts the authorities granted by the token from the given claims.
   *
   * @param claims the JWT claims
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
