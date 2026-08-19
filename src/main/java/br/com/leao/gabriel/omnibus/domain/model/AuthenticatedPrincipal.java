package br.com.leao.gabriel.omnibus.domain.model;

import java.util.Set;

/**
 * Represents the identity and authorities of a user who has been successfully authenticated,
 * independent of how authentication was performed or how the resulting token is issued.
 *
 * <p>This is intentionally decoupled from Spring Security's {@code UserDetails} so that the
 * domain and application layers never depend on a specific security framework.
 */
public record AuthenticatedPrincipal(String id, String email, Set<String> authorities) {}
