package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.exception.DailyTokenLimitExceededException;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpGeneratorPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpIssuanceRateLimiterPort;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificationOtpIssuerTest {

  private static final String USER_ID = "user-id";
  private static final String CODE = "482913";
  private static final String HASH = "hashed-code";
  private static final String TARGET_EMAIL = "new@example.com";

  @Mock private OtpGeneratorPort otpGenerator;

  @Mock private UserTokenRepositoryPort userTokenRepository;

  @Mock private OtpIssuanceRateLimiterPort rateLimiter;

  @Mock private UserToken activeToken;

  @Mock private UserToken revokedToken;

  private VerificationOtpIssuer issuer;

  @BeforeEach
  void setUp() {
    issuer = new VerificationOtpIssuer(otpGenerator, userTokenRepository, rateLimiter);
  }

  @Test
  @DisplayName("Should issue activation code successfully")
  void shouldIssueCode() {
    when(rateLimiter.incrementAndGet(USER_ID, OtpType.ACCOUNT_ACTIVATION)).thenReturn(1L);
    when(userTokenRepository.findActiveByUserId(USER_ID)).thenReturn(Optional.empty());
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    String result = issuer.issue(USER_ID, OtpType.ACCOUNT_ACTIVATION);

    assertThat(result).isEqualTo(CODE);

    verify(rateLimiter).incrementAndGet(USER_ID, OtpType.ACCOUNT_ACTIVATION);
    verify(userTokenRepository).findActiveByUserId(USER_ID);
    verify(userTokenRepository).flush();
    verify(otpGenerator).generateCode();
    verify(otpGenerator).hash(CODE);
    verify(userTokenRepository).save(any(UserToken.class));
  }

  @Test
  @DisplayName("Should revoke the active token before issuing a new token")
  void shouldRevokeActiveTokenBeforeIssuingNewToken() {
    when(rateLimiter.incrementAndGet(USER_ID, OtpType.ACCOUNT_ACTIVATION)).thenReturn(1L);
    when(userTokenRepository.findActiveByUserId(USER_ID)).thenReturn(Optional.of(activeToken));
    when(activeToken.revoke()).thenReturn(revokedToken);
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    issuer.issue(USER_ID, OtpType.ACCOUNT_ACTIVATION);

    verify(userTokenRepository).findActiveByUserId(USER_ID);
    verify(activeToken).revoke();
    verify(userTokenRepository).save(revokedToken);
    verify(userTokenRepository).flush();
    verify(userTokenRepository, times(2)).save(any(UserToken.class));
  }

  @Test
  @DisplayName("Should allow exactly three tokens")
  void shouldAllowThreeTokens() {
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);
    when(userTokenRepository.findActiveByUserId(USER_ID)).thenReturn(Optional.empty());

    when(rateLimiter.incrementAndGet(USER_ID, OtpType.ACCOUNT_ACTIVATION)).thenReturn(1L, 2L, 3L);

    assertThat(issuer.issue(USER_ID, OtpType.ACCOUNT_ACTIVATION)).isEqualTo(CODE);
    assertThat(issuer.issue(USER_ID, OtpType.ACCOUNT_ACTIVATION)).isEqualTo(CODE);
    assertThat(issuer.issue(USER_ID, OtpType.ACCOUNT_ACTIVATION)).isEqualTo(CODE);

    verify(userTokenRepository, times(3)).save(any(UserToken.class));
  }

  @Test
  @DisplayName("Should reject fourth token issuance")
  void shouldRejectFourthToken() {
    when(rateLimiter.incrementAndGet(USER_ID, OtpType.ACCOUNT_ACTIVATION)).thenReturn(4L);

    assertThrows(
        DailyTokenLimitExceededException.class,
        () -> issuer.issue(USER_ID, OtpType.ACCOUNT_ACTIVATION));

    verify(otpGenerator, never()).generateCode();
    verify(otpGenerator, never()).hash(any());
    verify(userTokenRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should persist generated hash instead of plain code")
  void shouldPersistHashedCode() {
    when(rateLimiter.incrementAndGet(USER_ID, OtpType.PASSWORD_RESET)).thenReturn(1L);
    when(userTokenRepository.findActiveByUserId(USER_ID)).thenReturn(Optional.empty());
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    issuer.issue(USER_ID, OtpType.PASSWORD_RESET);

    ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);

    verify(userTokenRepository).save(captor.capture());

    UserToken savedToken = captor.getValue();

    assertThat(savedToken.getCodeHash()).isEqualTo(HASH);
  }

  @Test
  @DisplayName("Should use 15 minutes as default expiration")
  void shouldUseDefaultExpiration() {
    when(rateLimiter.incrementAndGet(USER_ID, OtpType.ACCOUNT_ACTIVATION)).thenReturn(1L);
    when(userTokenRepository.findActiveByUserId(USER_ID)).thenReturn(Optional.empty());
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    Instant before = Instant.now().plusSeconds(15 * 60L);

    issuer.issue(USER_ID, OtpType.ACCOUNT_ACTIVATION);

    Instant after = Instant.now().plusSeconds(15 * 60L);

    ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);

    verify(userTokenRepository).save(captor.capture());

    UserToken savedToken = captor.getValue();

    assertThat(savedToken.getExpiresAt()).isBetween(before.minusSeconds(2), after.plusSeconds(2));
    assertThat(Duration.between(savedToken.getCreatedAt(), savedToken.getExpiresAt()))
        .isBetween(Duration.ofMinutes(15).minusSeconds(2), Duration.ofMinutes(15).plusSeconds(2));
  }

  @Test
  @DisplayName("Should use custom expiration")
  void shouldUseCustomExpiration() {
    long expirationMinutes = 30;

    when(rateLimiter.incrementAndGet(USER_ID, OtpType.PASSWORD_RESET)).thenReturn(1L);
    when(userTokenRepository.findActiveByUserId(USER_ID)).thenReturn(Optional.empty());
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    Instant before = Instant.now().plusSeconds(expirationMinutes * 60L);

    issuer.issue(USER_ID, OtpType.PASSWORD_RESET, expirationMinutes);

    Instant after = Instant.now().plusSeconds(expirationMinutes * 60L);

    ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);

    verify(userTokenRepository).save(captor.capture());

    UserToken savedToken = captor.getValue();

    assertThat(savedToken.getExpiresAt()).isBetween(before.minusSeconds(2), after.plusSeconds(2));
  }

  @Test
  @DisplayName("Should issue email change token with target email")
  void shouldIssueEmailChangeToken() {
    when(rateLimiter.incrementAndGet(USER_ID, OtpType.EMAIL_CHANGE)).thenReturn(1L);
    when(userTokenRepository.findActiveByUserId(USER_ID)).thenReturn(Optional.empty());
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    String result = issuer.issueForEmailChange(USER_ID, TARGET_EMAIL);

    assertThat(result).isEqualTo(CODE);

    ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);
    verify(userTokenRepository).save(captor.capture());

    UserToken savedToken = captor.getValue();

    assertThat(savedToken.getType()).isEqualTo(OtpType.EMAIL_CHANGE);
    assertThat(savedToken.getTargetEmail()).isEqualTo(TARGET_EMAIL);
    assertThat(savedToken.getCodeHash()).isEqualTo(HASH);
  }

  @Test
  @DisplayName("Should revoke an active token before issuing an email change token")
  void shouldRevokeActiveTokenBeforeIssuingEmailChangeToken() {
    when(rateLimiter.incrementAndGet(USER_ID, OtpType.EMAIL_CHANGE)).thenReturn(1L);
    when(userTokenRepository.findActiveByUserId(USER_ID)).thenReturn(Optional.of(activeToken));
    when(activeToken.revoke()).thenReturn(revokedToken);
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    issuer.issueForEmailChange(USER_ID, TARGET_EMAIL);

    verify(activeToken).revoke();
    verify(userTokenRepository).save(revokedToken);
    verify(userTokenRepository).flush();
    verify(userTokenRepository, times(2)).save(any(UserToken.class));
  }

  @Test
  @DisplayName("Should not generate token when rate limit is exceeded")
  void shouldNotGenerateTokenWhenLimitExceeded() {
    when(rateLimiter.incrementAndGet(USER_ID, OtpType.PASSWORD_RESET)).thenReturn(4L);

    assertThrows(
        DailyTokenLimitExceededException.class,
        () -> issuer.issue(USER_ID, OtpType.PASSWORD_RESET, 10));

    verifyNoInteractions(otpGenerator);
    verifyNoInteractions(userTokenRepository);
  }
}
