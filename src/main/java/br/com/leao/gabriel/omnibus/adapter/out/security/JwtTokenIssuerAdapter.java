package br.com.leao.gabriel.omnibus.adapter.out.security;

import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Issues signed JWT access tokens using HMAC-SHA256.
 */
@Component
public class JwtTokenIssuerAdapter implements TokenIssuerPort {

  private final SecretKey signingKey;
  private final long accessExpirationMinutes;
  private final long passwordResetExpirationMinutes;

  /**
   * Creates the JWT token issuer with the configured signing properties.
   *
   * @param secret the secret used to sign JWTs
   * @param accessExpirationMinutes the access token lifetime in minutes
   * @param passwordResetExpirationMinutes the password reset token lifetime in minutes
   */
  public JwtTokenIssuerAdapter(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expiration-minutes}") long accessExpirationMinutes,
      @Value("${jwt.password-reset-expiration-minutes}") long passwordResetExpirationMinutes) {

    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessExpirationMinutes = accessExpirationMinutes;
    this.passwordResetExpirationMinutes = passwordResetExpirationMinutes;
  }

  /**
   * Issues an access token for an authenticated principal.
   *
   * @param principal the authenticated user's identity and authorities
   * @return a signed access token
   */
  @Override
  public String issueAccessToken(AuthenticatedPrincipal principal) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(principal.id())
        .claim("email", principal.email())
        .claim("authorities", principal.authorities())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(accessExpirationMinutes, ChronoUnit.MINUTES)))
        .signWith(signingKey)
        .compact();
  }

  /**
   * Issues a short-lived token for password reset.
   *
   * @param userId the ID of the user authorised to reset their password
   * @return a signed short-lived password reset token
   */
  @Override
  public String issuePasswordResetToken(String userId) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userId)
        .claim("purpose", OtpType.PASSWORD_RESET)
        .claim("authorities", List.of("PASSWORD_RESET"))
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(passwordResetExpirationMinutes, ChronoUnit.MINUTES)))
        .signWith(signingKey)
        .compact();
  }
}
