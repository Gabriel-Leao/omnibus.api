package br.com.leao.gabriel.omnibus.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JwtTokenAdapterTest {

  private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-signing-key";
  private final JwtTokenIssuerAdapter issuer = new JwtTokenIssuerAdapter(SECRET, 30, 15);
  private final JwtTokenParser parser = new JwtTokenParser(SECRET);

  @Test
  void shouldIssueAndParseAccessToken() {
    String token =
        issuer.issueAccessToken(
            new AuthenticatedPrincipal("user-id", "user@example.com", Set.of("ROLE_CUSTOMER")));
    var claims = parser.parseClaims(token);

    assertThat(claims.getSubject()).isEqualTo("user-id");
    assertThat(claims.get("email", String.class)).isEqualTo("user@example.com");
    assertThat(parser.extractAuthorities(claims)).containsExactly("ROLE_CUSTOMER");
  }

  @Test
  void shouldIssuePasswordResetTokenWithRestrictedPurpose() {
    var claims = parser.parseClaims(issuer.issuePasswordResetToken("user-id"));

    assertThat(parser.extractPurpose(claims)).isEqualTo(OtpType.PASSWORD_RESET);
    assertThat(parser.extractAuthorities(claims)).containsExactly("PASSWORD_RESET");
  }
}
