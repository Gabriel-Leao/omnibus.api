package br.com.leao.gabriel.omnibus.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTokenTest {

  private static final String USER_ID = "user-id";
  private static final String CODE_HASH = "hashed-code";

  @Test
  @DisplayName("Should create an active token with zero attempts")
  void shouldCreateActiveToken() {
    Instant expiresAt = Instant.now().plusSeconds(900);

    UserToken token =
        UserToken.issue(USER_ID, CODE_HASH, OtpType.ACCOUNT_ACTIVATION, expiresAt);

    assertThat(token.getUserId()).isEqualTo(USER_ID);
    assertThat(token.getCodeHash()).isEqualTo(CODE_HASH);
    assertThat(token.getType()).isEqualTo(OtpType.ACCOUNT_ACTIVATION);
    assertThat(token.getAttempts()).isZero();
    assertThat(token.getTokenStatus()).isEqualTo(TokenStatus.ACTIVE);
    assertThat(token.isUsable()).isTrue();
  }

  @Test
  @DisplayName("Should reject EMAIL_CHANGE through generic token factory")
  void shouldRejectEmailChangeThroughGenericFactory() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            UserToken.issue(
                USER_ID,
                CODE_HASH,
                OtpType.EMAIL_CHANGE,
                Instant.now().plusSeconds(900)));
  }

  @Test
  @DisplayName("Should register failed attempts and revoke token at the limit")
  void shouldRevokeTokenAtMaximumAttempts() {
    UserToken token =
        UserToken.issue(
            USER_ID,
            CODE_HASH,
            OtpType.ACCOUNT_ACTIVATION,
            Instant.now().plusSeconds(900));

    UserToken firstFailure = token.registerFailedAttempt();
    UserToken secondFailure = firstFailure.registerFailedAttempt();
    UserToken thirdFailure = secondFailure.registerFailedAttempt();

    assertThat(firstFailure.getAttempts()).isEqualTo(1);
    assertThat(firstFailure.getTokenStatus()).isEqualTo(TokenStatus.ACTIVE);
    assertThat(secondFailure.getAttempts()).isEqualTo(2);
    assertThat(secondFailure.getTokenStatus()).isEqualTo(TokenStatus.ACTIVE);
    assertThat(thirdFailure.getAttempts()).isEqualTo(3);
    assertThat(thirdFailure.getTokenStatus()).isEqualTo(TokenStatus.REVOKED);
    assertThat(thirdFailure.isUsable()).isFalse();
  }

  @Test
  @DisplayName("Should reject another failed attempt after token is revoked")
  void shouldRejectFailedAttemptAfterRevocation() {
    UserToken token =
        UserToken.issue(
            USER_ID,
            CODE_HASH,
            OtpType.ACCOUNT_ACTIVATION,
            Instant.now().plusSeconds(900));

    UserToken revoked =
        token.registerFailedAttempt().registerFailedAttempt().registerFailedAttempt();

    assertThrows(InvalidVerificationCodeException.class, revoked::registerFailedAttempt);
  }

  @Test
  @DisplayName("Should not consider expired token usable")
  void shouldNotConsiderExpiredTokenUsable() {
    UserToken token =
        UserToken.issue(
            USER_ID,
            CODE_HASH,
            OtpType.ACCOUNT_ACTIVATION,
            Instant.now().minusSeconds(1));

    assertThat(token.isExpired()).isTrue();
    assertThat(token.isUsable()).isFalse();
  }

  @Test
  @DisplayName("Should mark an active token as used")
  void shouldMarkTokenAsUsed() {
    UserToken token =
        UserToken.issue(
            USER_ID,
            CODE_HASH,
            OtpType.ACCOUNT_ACTIVATION,
            Instant.now().plusSeconds(900));

    UserToken used = token.markUsed();

    assertThat(used.isUsed()).isTrue();
    assertThat(used.isUsable()).isFalse();
    assertThat(used.getUsedAt()).isNotNull();
  }

  @Test
  @DisplayName("Should create email change token with target email")
  void shouldCreateEmailChangeToken() {
    UserToken token =
        UserToken.issueForEmailChange(
            USER_ID,
            CODE_HASH,
            "new@example.com",
            Instant.now().plusSeconds(900));

    assertThat(token.getType()).isEqualTo(OtpType.EMAIL_CHANGE);
    assertThat(token.getTargetEmail()).isEqualTo("new@example.com");
  }
}
