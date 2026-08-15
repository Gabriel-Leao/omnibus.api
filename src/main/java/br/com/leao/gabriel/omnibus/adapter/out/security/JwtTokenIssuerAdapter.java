package br.com.leao.gabriel.omnibus.adapter.out.security;

import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Issues signed JWT access tokens using HMAC-SHA256.
 */
@Component
public class JwtTokenIssuerAdapter implements TokenIssuerPort {

  private final SecretKey signingKey;
  private final long expirationMinutes;

  public JwtTokenIssuerAdapter(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration-minutes:60}") long expirationMinutes) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationMinutes = expirationMinutes;
  }

  @Override
  public String issueAccessToken(AuthenticatedPrincipal principal) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(principal.id())
        .claim("email", principal.email())
        .claim("authorities", principal.authorities())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
        .signWith(signingKey)
        .compact();
  }
}