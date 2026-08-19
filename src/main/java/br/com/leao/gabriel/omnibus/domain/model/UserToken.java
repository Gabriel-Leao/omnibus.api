package br.com.leao.gabriel.omnibus.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.Getter;

/**
 * A single-use, time-bound verification code, identified only by the hash of its plain value. Used
 * to prove access to an email address before activating an account, resetting a password, or
 * confirming a new email address.
 */
@Getter
public class UserToken {

  /**
   * Maximum number of failed verification attempts before the code must be regenerated.
   */
  public static final int MAX_ATTEMPTS = 3;

  /**
   * Minimum time that must elapse before a new code can be requested for the same purpose.
   */
  public static final long RESEND_COOLDOWN_SECONDS = 60;

  private final Long id;
  private final String userId;
  private final String codeHash;
  private final TokenType type;
  private final String targetEmail;
  private final int attempts;
  private final OffsetDateTime expiresAt;
  private final OffsetDateTime createdAt;
  private final OffsetDateTime usedAt;

  private UserToken(
      Long id,
      String userId,
      String codeHash,
      TokenType type,
      String targetEmail,
      int attempts,
      OffsetDateTime expiresAt,
      OffsetDateTime createdAt,
      OffsetDateTime usedAt) {
    this.id = id;
    this.userId = Objects.requireNonNull(userId, "User id must not be null");
    this.codeHash = Objects.requireNonNull(codeHash, "Code hash must not be null");
    this.type = Objects.requireNonNull(type, "Type must not be null");
    this.targetEmail = targetEmail;
    this.attempts = attempts;
    this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration must not be null");
    this.createdAt = createdAt;
    this.usedAt = usedAt;
  }

  /**
   * Issues a new token for activation or password reset (no target email involved).
   */
  public static UserToken issue(
      String userId, String codeHash, TokenType type, OffsetDateTime expiresAt) {
    if (type == TokenType.EMAIL_CHANGE) {
      throw new IllegalArgumentException("Use issueForEmailChange(...) for EMAIL_CHANGE tokens");
    }
    return new UserToken(null, userId, codeHash, type, null, 0, expiresAt, null, null);
  }

  /**
   * Issues a new token specifically for confirming a new email address.
   */
  public static UserToken issueForEmailChange(
      String userId, String codeHash, String targetEmail, OffsetDateTime expiresAt) {
    Objects.requireNonNull(targetEmail, "Target email must not be null for EMAIL_CHANGE tokens");
    return new UserToken(
        null, userId, codeHash, TokenType.EMAIL_CHANGE, targetEmail, 0, expiresAt, null, null);
  }

  /**
   * Reconstructs a token from its persisted state.
   *
   * @param id          the persisted token identifier
   * @param userId      the identifier of the user associated with the token
   * @param codeHash    the hash of the verification code
   * @param type        the token type
   * @param targetEmail the email address being confirmed, when applicable
   * @param attempts    the number of failed verification attempts
   * @param expiresAt   the token expiration time
   * @param createdAt   the token creation time
   * @param usedAt      the time the token was consumed, or {@code null} if unused
   * @return a reconstructed user token
   * @throws NullPointerException if {@code id} or {@code createdAt} is {@code null}
   */
  public static UserToken reconstruct(
      Long id,
      String userId,
      String codeHash,
      TokenType type,
      String targetEmail,
      int attempts,
      OffsetDateTime expiresAt,
      OffsetDateTime createdAt,
      OffsetDateTime usedAt) {
    Objects.requireNonNull(id, "Id must not be null when reconstructing a persisted token");
    Objects.requireNonNull(createdAt, "CreatedAt must not be null when reconstructing");
    return new UserToken(
        id, userId, codeHash, type, targetEmail, attempts, expiresAt, createdAt, usedAt);
  }

  /**
   * Returns a copy of this token with the attempt counter incremented by one.
   */
  public UserToken incrementAttempts() {
    return new UserToken(
        id, userId, codeHash, type, targetEmail, attempts + 1, expiresAt, createdAt, usedAt);
  }

  /**
   * Returns a copy of this token marked as used, at the current instant.
   */
  public UserToken markUsed() {
    return new UserToken(
        id,
        userId,
        codeHash,
        type,
        targetEmail,
        attempts,
        expiresAt,
        createdAt,
        OffsetDateTime.now());
  }

  public boolean isExpired() {
    return OffsetDateTime.now().isAfter(expiresAt);
  }

  public boolean isUsed() {
    return usedAt != null;
  }

  public boolean isAttemptsExceeded() {
    return attempts >= MAX_ATTEMPTS;
  }

  /**
   * Whether this token is still eligible for verification.
   */
  public boolean isActive() {
    return !isUsed() && !isExpired() && !isAttemptsExceeded();
  }

  /**
   * Whether enough time has passed since issuance to allow requesting a new code.
   */
  public boolean isResendAllowed() {
    return createdAt.plusSeconds(RESEND_COOLDOWN_SECONDS).isBefore(OffsetDateTime.now());
  }
}
