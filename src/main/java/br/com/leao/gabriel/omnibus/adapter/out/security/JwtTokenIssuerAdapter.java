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

  public JwtTokenIssuerAdapter(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expiration-minutes}") long accessExpirationMinutes,
      @Value("${jwt.password-reset-expiration-minutes}") long passwordResetExpirationMinutes) {

    this.signingKey = Keys.hmacShaKeyFor(
        secret.getBytes(StandardCharsets.UTF_8)
    );
    this.accessExpirationMinutes = accessExpirationMinutes;
    this.passwordResetExpirationMinutes = passwordResetExpirationMinutes;
  }

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
   * @param userId the ID of the user authorised to reset their password
   * @return signed short-lived reset password token
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
