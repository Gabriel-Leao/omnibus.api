package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThatCode;
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
class ResendActivationCodeServiceTest {

  private static final String EMAIL = "gabriel@teste.com";
  private static final String CUSTOMER_ID = "customer-id";
  private static final String CODE = "482913";

  @Mock
  private CustomerRepositoryPort customerRepository;
  @Mock
  private VerificationOtpIssuer verificationOtpIssuer;
  @Mock
  private OtpSenderPort activationCodeSender;
  @Mock
  private UserTokenRepositoryPort userTokenRepository;
  @Mock
  private Customer customer;
  @Mock
  private UserToken token;

  private ResendActivationCodeService service;

  @BeforeEach
  void setUp() {
    service =
        new ResendActivationCodeService(
            customerRepository,
            verificationOtpIssuer,
            activationCodeSender,
            userTokenRepository);
  }

  @Test
  @DisplayName("Should resend activation code for an existing inactive customer")
  void shouldResendActivationCode() {
    givenInactiveCustomer();
    when(userTokenRepository.findLatestByUserIdAndType(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.empty());
    when(verificationOtpIssuer.issue(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION))
        .thenReturn(CODE);

    service.execute(EMAIL);

    verify(verificationOtpIssuer).issue(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION);
    verify(activationCodeSender).sendActivationCode(customer, CODE);
  }

  @Test
  @DisplayName("Should not reveal whether an email is registered")
  void shouldDoNothingWhenCustomerDoesNotExist() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThatCode(() -> service.execute(EMAIL)).doesNotThrowAnyException();

    verifyNoInteractions(verificationOtpIssuer, activationCodeSender, userTokenRepository);
  }

  @Test
  @DisplayName("Should not resend activation code for an already activated customer")
  void shouldDoNothingWhenCustomerIsAlreadyActivated() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(customer.isActivated()).thenReturn(true);

    assertThatCode(() -> service.execute(EMAIL)).doesNotThrowAnyException();

    verifyNoInteractions(verificationOtpIssuer, activationCodeSender, userTokenRepository);
  }

  @Test
  @DisplayName("Should reject resend while cooldown is active")
  void shouldRejectWhenResendCooldownIsActive() {
    givenInactiveCustomer();
    when(userTokenRepository.findLatestByUserIdAndType(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.of(token));
    when(token.isResendAllowed()).thenReturn(false);

    assertThrows(ResendCooldownActiveException.class, () -> service.execute(EMAIL));

    verify(verificationOtpIssuer, never()).issue(any(), any());
    verify(activationCodeSender, never()).sendActivationCode(any(), any());
  }

  @Test
  @DisplayName("Should resend when an existing token is past its cooldown")
  void shouldResendWhenCooldownHasElapsed() {
    givenInactiveCustomer();
    when(userTokenRepository.findLatestByUserIdAndType(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.of(token));
    when(token.isResendAllowed()).thenReturn(true);
    when(verificationOtpIssuer.issue(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION))
        .thenReturn(CODE);

    service.execute(EMAIL);

    verify(verificationOtpIssuer).issue(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION);
    verify(activationCodeSender).sendActivationCode(customer, CODE);
  }

  private void givenInactiveCustomer() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(customer.isActivated()).thenReturn(false);
  }
}
