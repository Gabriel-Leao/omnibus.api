package br.com.leao.gabriel.omnibus.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.exception.ResendCooldownActiveException;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpSenderPort;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendOtpServiceTest {

  private static final String EMAIL = "[gabriel@example.com](mailto:gabriel@example.com)";
  private static final String CUSTOMER_ID = "customer-id";
  private static final String CODE = "482913";

  @Mock private CustomerRepositoryPort customerRepository;
  @Mock private VerificationOtpIssuer verificationOtpIssuer;
  @Mock private OtpSenderPort otpSender;
  @Mock private UserTokenRepositoryPort userTokenRepository;
  @Mock private Customer customer;
  @Mock private UserToken token;

  private SendOtpService service;

  @BeforeEach
  void setUp() {
    service =
        new SendOtpService(
            customerRepository, verificationOtpIssuer, otpSender, userTokenRepository);
  }

  @Test
  @DisplayName("Should send an activation OTP to an inactive customer")
  void shouldSendActivationOtp() {
    givenCustomer(false);

    when(userTokenRepository.findLatestByUserIdAndType(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.empty());

    when(verificationOtpIssuer.issue(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION)).thenReturn(CODE);

    service.execute(EMAIL, OtpType.ACCOUNT_ACTIVATION);

    verify(verificationOtpIssuer).issue(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION);
    verify(otpSender).sendOtp(customer, CODE, OtpType.ACCOUNT_ACTIVATION);
  }

  @Test
  @DisplayName("Should send a password reset OTP to an active customer")
  void shouldSendPasswordResetOtp() {
    givenCustomer(true);

    when(userTokenRepository.findLatestByUserIdAndType(CUSTOMER_ID, OtpType.PASSWORD_RESET))
        .thenReturn(Optional.empty());

    when(verificationOtpIssuer.issue(CUSTOMER_ID, OtpType.PASSWORD_RESET)).thenReturn(CODE);

    service.execute(EMAIL, OtpType.PASSWORD_RESET);

    verify(verificationOtpIssuer).issue(CUSTOMER_ID, OtpType.PASSWORD_RESET);
    verify(otpSender).sendOtp(customer, CODE, OtpType.PASSWORD_RESET);
  }

  @Test
  @DisplayName("Should do nothing when the customer does not exist")
  void shouldDoNothingWhenCustomerDoesNotExist() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    service.execute(EMAIL, OtpType.PASSWORD_RESET);

    verifyNoInteractions(verificationOtpIssuer, otpSender, userTokenRepository);
  }

  @Test
  @DisplayName("Should do nothing when the OTP type is not applicable to the account state")
  void shouldDoNothingWhenOtpTypeIsNotApplicable() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(customer.isActivated()).thenReturn(true);

    service.execute(EMAIL, OtpType.ACCOUNT_ACTIVATION);

    verify(customerRepository).findByEmail(EMAIL);
    verify(customer).isActivated();
    verifyNoInteractions(verificationOtpIssuer, otpSender, userTokenRepository);
  }

  @Test
  @DisplayName("Should reject a resend while the cooldown is active")
  void shouldRejectWhenCooldownIsActive() {
    givenCustomer(true);

    when(userTokenRepository.findLatestByUserIdAndType(CUSTOMER_ID, OtpType.PASSWORD_RESET))
        .thenReturn(Optional.of(token));

    when(token.isResendAllowed()).thenReturn(false);

    assertThrows(
        ResendCooldownActiveException.class, () -> service.execute(EMAIL, OtpType.PASSWORD_RESET));

    verify(verificationOtpIssuer, never()).issue(any(), any());
    verify(otpSender, never()).sendOtp(any(), any(), any());
  }

  private void givenCustomer(boolean activated) {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(customer.isActivated()).thenReturn(activated);
  }
}
