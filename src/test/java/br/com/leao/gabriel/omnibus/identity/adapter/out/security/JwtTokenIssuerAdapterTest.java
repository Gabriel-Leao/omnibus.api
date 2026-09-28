package br.com.leao.gabriel.omnibus.identity.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.leao.gabriel.omnibus.identity.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.kernel.adapter.out.security.JwtTokenParser;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JwtTokenIssuerAdapterTest {

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

    // extractPurpose returns the raw claim as a String — kernel has no OtpType to compare
    // against. The value still reads "PASSWORD_RESET" because JwtTokenIssuerAdapter writes
    // OtpType.PASSWORD_RESET as a claim, and Jackson serialises an enum to its name() in JSON.
    assertThat(parser.extractPurpose(claims)).isEqualTo("PASSWORD_RESET");
    assertThat(parser.extractAuthorities(claims)).containsExactly("PASSWORD_RESET");
  }
}
