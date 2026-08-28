package br.com.leao.gabriel.omnibus.domain.model;

import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * A single-use, time-bound verification code, identified only by the hash of its plain value.
 *
 * <p>Tokens can be used for account activation, password reset, email change, or other
 * verification purposes.
 */
@Getter
public class UserToken {

  /**
   * Maximum number of verification attempts allowed for the token.
   */
  public static final int MAX_ATTEMPTS = 3;

  /**
   * Minimum time that must elapse before a new code can be requested for the same purpose.
   */
  public static final long RESEND_COOLDOWN_SECONDS = 60;

  private final Long id;
  private final String userId;
  private final String codeHash;
  private final OtpType type;
  private final String targetEmail;
  private final int attempts;
  private final TokenStatus tokenStatus;
  private final Instant expiresAt;
  private final Instant createdAt;
  private final Instant usedAt;
  private final Instant revokedAt;

  private UserToken(
      Long id,
      String userId,
      String codeHash,
      OtpType type,
      String targetEmail,
      int attempts,
      TokenStatus tokenStatus,
      Instant expiresAt,
      Instant createdAt,
      Instant usedAt,
      Instant revokedAt) {

    this.id = id;
    this.userId = Objects.requireNonNull(userId, "User id must not be null");
    this.codeHash = Objects.requireNonNull(codeHash, "Code hash must not be null");
    this.type = Objects.requireNonNull(type, "Type must not be null");
    this.targetEmail = targetEmail;
    this.attempts = attempts;
    this.tokenStatus = Objects.requireNonNull(tokenStatus, "Status must not be null");
    this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration must not be null");
    this.createdAt = createdAt;
    this.usedAt = usedAt;
    this.revokedAt = revokedAt;
  }

  /**
   * Issues a new token for activation or password reset.
   */
  public static UserToken issue(String userId, String codeHash, OtpType type, Instant expiresAt) {

    if (type == OtpType.EMAIL_CHANGE) {
      throw new IllegalArgumentException("Use issueForEmailChange(...) for EMAIL_CHANGE tokens");
    }

    return new UserToken(
        null,
        userId,
        codeHash,
        type,
        null,
        0,
        TokenStatus.ACTIVE,
        expiresAt,
        Instant.now(),
        null,
        null);
  }

  /**
   * Issues a new token specifically for confirming a new email address.
   */
  public static UserToken issueForEmailChange(
      String userId, String codeHash, String targetEmail, Instant expiresAt) {

    Objects.requireNonNull(targetEmail, "Target email must not be null for EMAIL_CHANGE tokens");

    return new UserToken(
        null,
        userId,
        codeHash,
        OtpType.EMAIL_CHANGE,
        targetEmail,
        0,
        TokenStatus.ACTIVE,
        expiresAt,
        Instant.now(),
        null,
        null);
  }

  /**
   * Reconstructs a token from its persisted state.
   */
  public static UserToken reconstruct(
      Long id,
      String userId,
      String codeHash,
      OtpType type,
      String targetEmail,
      int attempts,
      TokenStatus status,
      Instant expiresAt,
      Instant createdAt,
      Instant usedAt,
      Instant revokedAt) {

    Objects.requireNonNull(id, "Id must not be null when reconstructing a persisted token");

    Objects.requireNonNull(createdAt, "CreatedAt must not be null when reconstructing");

    return new UserToken(
        id,
        userId,
        codeHash,
        type,
        targetEmail,
        attempts,
        status,
        expiresAt,
        createdAt,
        usedAt,
        revokedAt);
  }

  /**
   * Returns a copy of this token with one additional verification attempt.
   *
   * <p>When the maximum number of attempts is reached, the token is revoked immediately.
   */
  public UserToken registerFailedAttempt() {
    if (!isUsable()) {
      throw new InvalidVerificationCodeException();
    }

    int newAttempts = attempts + 1;

    TokenStatus newStatus =
        newAttempts >= MAX_ATTEMPTS ? TokenStatus.REVOKED : TokenStatus.ACTIVE;

    return new UserToken(
        id,
        userId,
        codeHash,
        type,
        targetEmail,
        newAttempts,
        newStatus,
        expiresAt,
        createdAt,
        usedAt,
        revokedAt);
  }

  /**
   * Marks this token as used after a successful verification attempt.
   */
  public UserToken markUsed() {
    if (!isUsable()) {
      throw new InvalidVerificationCodeException();
    }

    return new UserToken(
        id,
        userId,
        codeHash,
        type,
        targetEmail,
        attempts + 1,
        TokenStatus.USED,
        expiresAt,
        createdAt,
        Instant.now(),
        revokedAt);
  }

  /**
   * Returns whether this token has been revoked.
   *
   * @return {@code true} when the token is revoked
   */
  public boolean isRevoked() {
    return tokenStatus == TokenStatus.REVOKED;
  }

  /**
   * Returns a copy of this token marked as revoked.
   *
   * @return the revoked token
   */
  public UserToken revoke() {
    if (isRevoked()) {
      return this;
    }

    return new UserToken(
        id,
        userId,
        codeHash,
        type,
        targetEmail,
        attempts,
        TokenStatus.REVOKED,
        expiresAt,
        createdAt,
        usedAt,
        Instant.now());
  }

  /**
   * Marks this token as expired when its expiration time has elapsed.
   */
  public UserToken expire() {
    if (tokenStatus != TokenStatus.ACTIVE || !isExpiredByTime()) {
      return this;
    }

    return new UserToken(
        id,
        userId,
        codeHash,
        type,
        targetEmail,
        attempts,
        TokenStatus.EXPIRED,
        expiresAt,
        createdAt,
        usedAt,
        revokedAt);
  }

  /**
   * Returns whether the expiration time has elapsed.
   *
   * <p>This check is independent of the persisted status so an expired token cannot be used
   * even if the scheduled expiration job has not run yet.
   */
  public boolean isExpired() {
    return isExpiredByTime() || tokenStatus == TokenStatus.EXPIRED;
  }

  private boolean isExpiredByTime() {
    return !Instant.now().isBefore(expiresAt);
  }

  /**
   * Returns whether this token has already been used.
   *
   * @return {@code true} when the token has been used
   */
  public boolean isUsed() {
    return tokenStatus == TokenStatus.USED;
  }

  /**
   * Returns whether the maximum number of verification attempts has been reached.
   *
   * @return {@code true} when the maximum attempt count has been reached
   */
  public boolean isAttemptsExceeded() {
    return attempts >= MAX_ATTEMPTS;
  }

  /**
   * Whether this token can currently be used for verification.
   */
  public boolean isUsable() {
    return tokenStatus == TokenStatus.ACTIVE && !isExpiredByTime() && !isAttemptsExceeded();
  }

  /**
   * Whether enough time has passed since issuance to allow requesting a new code.
   */
  public boolean isResendAllowed() {
    return !Instant.now().isBefore(createdAt.plusSeconds(RESEND_COOLDOWN_SECONDS));
  }
}
