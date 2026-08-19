package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;

/**
 * Output port for issuing an access token for an authenticated principal.
 *
 * <p>The domain only knows that a token can be issued — it has no knowledge of the concrete
 * token format (JWT, opaque, etc.) or the signing mechanism used.
 */
public interface TokenIssuerPort {

  /**
   * Issues a signed access token representing the given authenticated principal.
   *
   * @param principal the authenticated user's identity and authorities
   * @return a signed, time-bound access token
   */
  String issueAccessToken(AuthenticatedPrincipal principal);
}
