package br.com.leao.gabriel.omnibus.adapter.out.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Parses and validates JWT access tokens issued by {@link JwtTokenIssuerAdapter}.
 */
@Component
public class JwtTokenParser {

  private final SecretKey signingKey;

  public JwtTokenParser(@Value("${jwt.secret}") String secret) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
  }

  /**
   * Parses the token and returns its claims.
   *
   * @throws JwtException if the token is invalid, malformed, or expired
   */
  public Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }

  @SuppressWarnings("unchecked")
  public List<String> extractAuthorities(Claims claims) {
    return (List<String>) claims.get("authorities", List.class);
  }
}