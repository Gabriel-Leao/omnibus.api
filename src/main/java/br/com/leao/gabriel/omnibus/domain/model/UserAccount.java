package br.com.leao.gabriel.omnibus.domain.model;

import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Base domain model for user accounts.
 */
@Getter
public abstract class UserAccount {

  private final String id;
  private final String name;
  private final String email;
  private final String passwordHash;
  private final UserStatus status;
  private final AccountType accountType;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final Instant deletedAt;

  /**
   * Handles the UserAccount operation.
   */
  protected UserAccount(
      String id,
      String name,
      String email,
      String passwordHash,
      UserStatus status,
      AccountType accountType,
      Instant createdAt,
      Instant updatedAt,
      Instant deletedAt) {
    this.id = id;
    this.name = Objects.requireNonNull(name, "Name must not be null");
    this.email = Objects.requireNonNull(email, "Email must not be null");
    this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash must not be null");
    this.status = Objects.requireNonNull(status, "Status must not be null");
    this.accountType = Objects.requireNonNull(accountType, "Account type must not be null");
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.deletedAt = validateDeletedAtConsistency(status, deletedAt);
  }

  private static Instant validateDeletedAtConsistency(UserStatus status, Instant deletedAt) {
    boolean isPendingDeletion = status == UserStatus.PENDING_DELETION;
    if (isPendingDeletion == (deletedAt == null)) {
      throw new IllegalStateException(
          "deletedAt must be set if and only if status is PENDING_DELETION");
    }
    return deletedAt;
  }
}
