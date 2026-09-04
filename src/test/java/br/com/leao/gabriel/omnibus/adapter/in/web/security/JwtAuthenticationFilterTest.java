package br.com.leao.gabriel.omnibus.adapter.in.web.security;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.leao.gabriel.omnibus.adapter.out.security.JwtTokenIssuerAdapter;
import br.com.leao.gabriel.omnibus.adapter.out.security.JwtTokenParser;
import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

  private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-signing-key";

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldAuthenticateRequestWithValidBearerToken() throws Exception {
    var issuer = new JwtTokenIssuerAdapter(SECRET, 30, 15);
    var filter = new JwtAuthenticationFilter(new JwtTokenParser(SECRET));
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
    var filter = new JwtAuthenticationFilter(new JwtTokenParser(SECRET));

    filter.doFilter(
        new MockHttpServletRequest(), new MockHttpServletResponse(), (request, response) -> {});

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }
}
