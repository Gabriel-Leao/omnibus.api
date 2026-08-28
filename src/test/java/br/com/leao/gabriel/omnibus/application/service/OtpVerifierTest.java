package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.domain.exception.VerificationAttemptsExceededException;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpGeneratorPort;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OtpVerifierTest {

  private static final String EMAIL = "gabriel@example.com";
  private static final String CUSTOMER_ID = "customer-id";
  private static final String CODE = "482913";
  private static final String CODE_HASH = "hashed-code";

  @Mock private CustomerRepositoryPort customerRepository;
  @Mock private UserTokenRepositoryPort userTokenRepository;
  @Mock private OtpGeneratorPort otpGenerator;
  @Mock private Customer customer;
  @Mock private UserToken token;
  @Mock private UserToken usedToken;

  private OtpVerifier verifier;

  @BeforeEach
  void setUp() {
    verifier = new OtpVerifier(customerRepository, userTokenRepository, otpGenerator);
  }

  @Test
  @DisplayName("Should verify a valid OTP and mark the token as used")
  void shouldVerifyValidOtp() {
    givenUsableToken();
    when(otpGenerator.hash(CODE)).thenReturn(CODE_HASH);
    when(token.getCodeHash()).thenReturn(CODE_HASH);
    when(token.markUsed()).thenReturn(usedToken);

    Customer result = verifier.verify(EMAIL, CODE, OtpType.ACCOUNT_ACTIVATION);

    assertThat(result).isSameAs(customer);
    verify(token).markUsed();
    verify(userTokenRepository).save(usedToken);
  }

  @Test
  @DisplayName("Should reject an OTP when the customer cannot use that OTP type")
  void shouldRejectWhenCustomerCannotUseOtp() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(customer.canUseOtp(OtpType.ACCOUNT_ACTIVATION)).thenReturn(false);

    assertThrows(
        InvalidVerificationCodeException.class,
        () -> verifier.verify(EMAIL, CODE, OtpType.ACCOUNT_ACTIVATION));

    verifyNoInteractions(otpGenerator);
    verifyNoInteractions(userTokenRepository);
  }

  @Test
  @DisplayName("Should record a failed attempt when the OTP is incorrect")
  void shouldRecordFailedAttemptForIncorrectOtp() {
    givenUsableToken();
    when(otpGenerator.hash(CODE)).thenReturn("different-hash");
    when(token.getCodeHash()).thenReturn(CODE_HASH);
    when(token.registerFailedAttempt()).thenReturn(usedToken);
    when(usedToken.isAttemptsExceeded()).thenReturn(false);

    assertThrows(
        InvalidVerificationCodeException.class,
        () -> verifier.verify(EMAIL, CODE, OtpType.ACCOUNT_ACTIVATION));

    verify(token).registerFailedAttempt();
    verify(userTokenRepository).save(usedToken);
    verify(token, never()).markUsed();
  }

  @Test
  @DisplayName("Should reject an unusable token before hashing the submitted OTP")
  void shouldRejectUnusableToken() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(customer.canUseOtp(OtpType.ACCOUNT_ACTIVATION)).thenReturn(true);
    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(userTokenRepository.findLatestByUserIdAndType(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.of(token));
    when(token.isUsable()).thenReturn(false);
    when(token.isAttemptsExceeded()).thenReturn(false);

    assertThrows(
        InvalidVerificationCodeException.class,
        () -> verifier.verify(EMAIL, CODE, OtpType.ACCOUNT_ACTIVATION));

    verifyNoInteractions(otpGenerator);
    verify(userTokenRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should report when the maximum number of attempts is exceeded")
  void shouldReportExceededAttempts() {
    givenUsableToken();
    when(otpGenerator.hash(CODE)).thenReturn("different-hash");
    when(token.getCodeHash()).thenReturn(CODE_HASH);
    when(token.registerFailedAttempt()).thenReturn(usedToken);
    when(usedToken.isAttemptsExceeded()).thenReturn(true);

    assertThrows(
        VerificationAttemptsExceededException.class,
        () -> verifier.verify(EMAIL, CODE, OtpType.ACCOUNT_ACTIVATION));

    verify(userTokenRepository).save(usedToken);
  }

  private void givenUsableToken() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(customer.canUseOtp(OtpType.ACCOUNT_ACTIVATION)).thenReturn(true);
    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(userTokenRepository.findLatestByUserIdAndType(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.of(token));
    when(token.isUsable()).thenReturn(true);
  }
}
