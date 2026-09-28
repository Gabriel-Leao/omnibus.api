package br.com.leao.gabriel.omnibus.kernel.adapter.in.web.security;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.leao.gabriel.omnibus.identity.adapter.out.security.JwtTokenIssuerAdapter;
import br.com.leao.gabriel.omnibus.identity.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.kernel.adapter.out.security.JwtTokenParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-signing-key";

  private final JwtTokenIssuerAdapter issuer = new JwtTokenIssuerAdapter(SECRET, 30, 15);
  private final JwtAuthenticationFilter filter =
      new JwtAuthenticationFilter(new JwtTokenParser(SECRET));

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldAuthenticateRequestWithValidBearerToken() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader(
        "Authorization",
        "Bearer "
            + issuer.issueAccessToken(
                new AuthenticatedPrincipal(
                    "user-id", "user@example.com", Set.of("ROLE_CUSTOMER"))));

    filter.doFilter(
        request, new MockHttpServletResponse(), (ignoredRequest, ignoredResponse) -> {});

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication.getName()).isEqualTo("user-id");
    assertThat(authentication.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_CUSTOMER");
  }

  @Test
  void shouldLeaveContextEmptyWithoutBearerToken() throws Exception {
    filter.doFilter(
        new MockHttpServletRequest(), new MockHttpServletResponse(), (request, response) -> {});

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  /**
   * Covers the branch touched when {@code extractPurpose} was changed to return a plain
   * {@code String} instead of identity's {@code OtpType}: a token correctly carrying the
   * PASSWORD_RESET authority and a matching purpose claim must still authenticate.
   */
  @Test
  void shouldAuthenticateValidPasswordResetToken() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + issuer.issuePasswordResetToken("user-id"));

    filter.doFilter(
        request, new MockHttpServletResponse(), (ignoredRequest, ignoredResponse) -> {});

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getAuthorities())
        .extracting("authority")
        .containsExactly("PASSWORD_RESET");
  }

  /**
   * A token declaring the PASSWORD_RESET authority but a different (or absent) purpose claim
   * must be rejected. This is the safety check the purpose comparison exists for, now expressed
   * with plain strings instead of {@code OtpType}.
   */
  @Test
  void shouldRejectPasswordResetAuthorityWithMismatchedPurpose() throws Exception {
    SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    Instant now = Instant.now();
    String tamperedToken =
        Jwts.builder()
            .subject("user-id")
            .claim("purpose", "SOMETHING_ELSE")
            .claim("authorities", List.of("PASSWORD_RESET"))
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(15, ChronoUnit.MINUTES)))
            .signWith(signingKey)
            .compact();

    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer " + tamperedToken);

    filter.doFilter(
        request, new MockHttpServletResponse(), (ignoredRequest, ignoredResponse) -> {});

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
