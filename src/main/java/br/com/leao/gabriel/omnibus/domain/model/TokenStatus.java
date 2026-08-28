package br.com.leao.gabriel.omnibus.domain.model;

/**
 * Represents the lifecycle status of a user token.
 */
public enum TokenStatus {
  ACTIVE,
  USED,
  REVOKED,
  EXPIRED
}
