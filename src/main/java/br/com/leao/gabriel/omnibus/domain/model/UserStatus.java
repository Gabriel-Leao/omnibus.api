package br.com.leao.gabriel.omnibus.domain.model;

/**
 * Defines the lifecycle statuses of user accounts.
 */
public enum UserStatus {
  ACTIVE,
  PENDING_ACTIVATION,
  PENDING_DELETION,
  SUSPENDED,
  BANNED
}
