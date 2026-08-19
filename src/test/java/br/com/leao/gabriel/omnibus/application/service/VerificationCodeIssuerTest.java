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
import br.com.leao.gabriel.omnibus.domain.model.TokenType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpGeneratorPort;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuanceRateLimiterPort;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificationCodeIssuerTest {

  private static final String USER_ID = "user-id";
  private static final String CODE = "482913";
  private static final String HASH = "hashed-code";

  @Mock private OtpGeneratorPort otpGenerator;

  @Mock private UserTokenRepositoryPort userTokenRepository;

  @Mock private TokenIssuanceRateLimiterPort rateLimiter;

  private VerificationCodeIssuer issuer;

  @BeforeEach
  void setUp() {
    issuer = new VerificationCodeIssuer(otpGenerator, userTokenRepository, rateLimiter);
  }

  @Test
  @DisplayName("Should issue activation code successfully")
  void shouldIssueCode() {
    when(rateLimiter.incrementAndGet(USER_ID, TokenType.ACCOUNT_ACTIVATION)).thenReturn(1L);
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    String result = issuer.issue(USER_ID, TokenType.ACCOUNT_ACTIVATION);

    assertThat(result).isEqualTo(CODE);

    verify(rateLimiter).incrementAndGet(USER_ID, TokenType.ACCOUNT_ACTIVATION);
    verify(otpGenerator).generateCode();
    verify(otpGenerator).hash(CODE);
    verify(userTokenRepository).save(any(UserToken.class));
  }

  @Test
  @DisplayName("Should allow exactly three tokens")
  void shouldAllowThreeTokens() {
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    when(rateLimiter.incrementAndGet(USER_ID, TokenType.ACCOUNT_ACTIVATION)).thenReturn(1L, 2L, 3L);

    assertThat(issuer.issue(USER_ID, TokenType.ACCOUNT_ACTIVATION)).isEqualTo(CODE);

    assertThat(issuer.issue(USER_ID, TokenType.ACCOUNT_ACTIVATION)).isEqualTo(CODE);

    assertThat(issuer.issue(USER_ID, TokenType.ACCOUNT_ACTIVATION)).isEqualTo(CODE);

    verify(userTokenRepository, times(3)).save(any(UserToken.class));
  }

  @Test
  @DisplayName("Should reject fourth token issuance")
  void shouldRejectFourthToken() {
    when(rateLimiter.incrementAndGet(USER_ID, TokenType.ACCOUNT_ACTIVATION)).thenReturn(4L);

    assertThrows(
        DailyTokenLimitExceededException.class,
        () -> issuer.issue(USER_ID, TokenType.ACCOUNT_ACTIVATION));

    verify(otpGenerator, never()).generateCode();
    verify(otpGenerator, never()).hash(any());
    verify(userTokenRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should persist generated hash instead of plain code")
  void shouldPersistHashedCode() {
    when(rateLimiter.incrementAndGet(USER_ID, TokenType.PASSWORD_RESET)).thenReturn(1L);
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    issuer.issue(USER_ID, TokenType.PASSWORD_RESET);

    ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);

    verify(userTokenRepository).save(captor.capture());

    UserToken savedToken = captor.getValue();

    assertThat(savedToken.getCodeHash()).isEqualTo(HASH);
  }

  @Test
  @DisplayName("Should use 15 minutes as default expiration")
  void shouldUseDefaultExpiration() {
    when(rateLimiter.incrementAndGet(USER_ID, TokenType.ACCOUNT_ACTIVATION)).thenReturn(1L);
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    OffsetDateTime before = OffsetDateTime.now().plusMinutes(15);

    issuer.issue(USER_ID, TokenType.ACCOUNT_ACTIVATION);

    OffsetDateTime after = OffsetDateTime.now().plusMinutes(15);

    ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);

    verify(userTokenRepository).save(captor.capture());

    UserToken savedToken = captor.getValue();

    assertThat(savedToken.getExpiresAt()).isBetween(before.minusSeconds(2), after.plusSeconds(2));
  }

  @Test
  @DisplayName("Should use custom expiration")
  void shouldUseCustomExpiration() {
    long expirationMinutes = 30;

    when(rateLimiter.incrementAndGet(USER_ID, TokenType.PASSWORD_RESET)).thenReturn(1L);
    when(otpGenerator.generateCode()).thenReturn(CODE);
    when(otpGenerator.hash(CODE)).thenReturn(HASH);

    OffsetDateTime before = OffsetDateTime.now().plusMinutes(expirationMinutes);

    issuer.issue(USER_ID, TokenType.PASSWORD_RESET, expirationMinutes);

    OffsetDateTime after = OffsetDateTime.now().plusMinutes(expirationMinutes);

    ArgumentCaptor<UserToken> captor = ArgumentCaptor.forClass(UserToken.class);

    verify(userTokenRepository).save(captor.capture());

    UserToken savedToken = captor.getValue();

    assertThat(savedToken.getExpiresAt()).isBetween(before.minusSeconds(2), after.plusSeconds(2));
  }

  @Test
  @DisplayName("Should not generate token when rate limit is exceeded")
  void shouldNotGenerateTokenWhenLimitExceeded() {
    when(rateLimiter.incrementAndGet(USER_ID, TokenType.PASSWORD_RESET)).thenReturn(4L);

    assertThrows(
        DailyTokenLimitExceededException.class,
        () -> issuer.issue(USER_ID, TokenType.PASSWORD_RESET, 10));

    verifyNoInteractions(otpGenerator);
    verifyNoInteractions(userTokenRepository);
  }
}
