package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.domain.exception.VerificationAttemptsExceededException;
import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.TokenType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpGeneratorPort;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivateCustomerServiceTest {

  private static final String EMAIL = "gabriel@teste.com";
  private static final String CUSTOMER_ID = "customer-id";
  private static final String CODE = "482913";
  private static final String CODE_HASH = "hashed-code";
  private static final String ACCESS_TOKEN = "access-token";

  @Mock
  private CustomerRepositoryPort customerRepository;

  @Mock
  private UserTokenRepositoryPort userTokenRepository;

  @Mock
  private OtpGeneratorPort otpGenerator;

  @Mock
  private TokenIssuerPort tokenIssuer;

  @Mock
  private PrincipalFactory principalFactory;

  @Mock
  private Customer customer;

  @Mock
  private Customer activatedCustomer;

  @Mock
  private UserToken token;

  @Mock
  private AuthenticatedPrincipal principal;

  private ActivateCustomerService service;

  @BeforeEach
  void setUp() {
    service =
        new ActivateCustomerService(
            customerRepository,
            userTokenRepository,
            otpGenerator,
            tokenIssuer,
            principalFactory);
  }

  @Test
  @DisplayName("Should activate customer and issue access token with valid code")
  void shouldActivateCustomerWithValidCode() {
    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));

    when(userTokenRepository.findLatestByUserIdAndType(
        CUSTOMER_ID, TokenType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.of(token));

    when(token.isActive()).thenReturn(true);
    when(otpGenerator.hash(CODE)).thenReturn(CODE_HASH);
    when(token.getCodeHash()).thenReturn(CODE_HASH);

    when(customer.activate()).thenReturn(activatedCustomer);
    when(customerRepository.save(any(Customer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(token.markUsed()).thenReturn(token);

    when(principalFactory.forCustomer(activatedCustomer)).thenReturn(principal);
    when(tokenIssuer.issueAccessToken(principal)).thenReturn(ACCESS_TOKEN);

    String result = service.execute(EMAIL, CODE);

    assertThat(result).isEqualTo(ACCESS_TOKEN);

    verify(customerRepository).save(activatedCustomer);
    verify(userTokenRepository).save(token);
    verify(token).markUsed();
    verify(principalFactory).forCustomer(activatedCustomer);
    verify(tokenIssuer).issueAccessToken(principal);

    verify(otpGenerator).hash(CODE);
  }

  @Test
  @DisplayName("Should reject activation when customer does not exist")
  void shouldRejectWhenCustomerDoesNotExist() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThrows(
        InvalidVerificationCodeException.class,
        () -> service.execute(EMAIL, CODE));

    verifyNoInteractions(userTokenRepository, otpGenerator, tokenIssuer, principalFactory);
  }

  @Test
  @DisplayName("Should reject activation when activation token does not exist")
  void shouldRejectWhenTokenDoesNotExist() {
    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));

    when(userTokenRepository.findLatestByUserIdAndType(
        CUSTOMER_ID, TokenType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.empty());

    assertThrows(
        InvalidVerificationCodeException.class,
        () -> service.execute(EMAIL, CODE));

    verifyNoInteractions(otpGenerator, tokenIssuer, principalFactory);
  }

  @Test
  @DisplayName("Should reject when token is inactive but attempts were not exceeded")
  void shouldRejectInactiveToken() {
    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));

    when(userTokenRepository.findLatestByUserIdAndType(
        CUSTOMER_ID, TokenType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.of(token));

    when(token.isActive()).thenReturn(false);
    when(token.isAttemptsExceeded()).thenReturn(false);

    assertThrows(
        InvalidVerificationCodeException.class,
        () -> service.execute(EMAIL, CODE));

    verifyNoInteractions(otpGenerator, tokenIssuer, principalFactory);
    verify(userTokenRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should reject when maximum verification attempts have been exceeded")
  void shouldRejectWhenAttemptsExceeded() {
    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));

    when(userTokenRepository.findLatestByUserIdAndType(
        CUSTOMER_ID, TokenType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.of(token));

    when(token.isActive()).thenReturn(false);
    when(token.isAttemptsExceeded()).thenReturn(true);

    assertThrows(
        VerificationAttemptsExceededException.class,
        () -> service.execute(EMAIL, CODE));

    verifyNoInteractions(otpGenerator, tokenIssuer, principalFactory);
    verify(userTokenRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should increment attempts when submitted code is invalid")
  void shouldIncrementAttemptsWhenCodeIsInvalid() {
    UserToken incrementedToken = mock(UserToken.class);

    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));

    when(userTokenRepository.findLatestByUserIdAndType(
        CUSTOMER_ID, TokenType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.of(token));

    when(token.isActive()).thenReturn(true);
    when(otpGenerator.hash(CODE)).thenReturn("wrong-hash");
    when(token.getCodeHash()).thenReturn(CODE_HASH);
    when(token.incrementAttempts()).thenReturn(incrementedToken);

    assertThrows(
        InvalidVerificationCodeException.class,
        () -> service.execute(EMAIL, CODE));

    verify(token).incrementAttempts();
    verify(userTokenRepository).save(incrementedToken);

    verify(customerRepository, never()).save(any());
    verify(tokenIssuer, never()).issueAccessToken(any());
    verify(principalFactory, never()).forCustomer(any());
  }

  @Test
  @DisplayName("Should not increment attempts when token is already inactive")
  void shouldNotIncrementAttemptsWhenTokenIsInactive() {
    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));

    when(userTokenRepository.findLatestByUserIdAndType(
        CUSTOMER_ID, TokenType.ACCOUNT_ACTIVATION))
        .thenReturn(Optional.of(token));

    when(token.isActive()).thenReturn(false);
    when(token.isAttemptsExceeded()).thenReturn(false);

    assertThrows(
        InvalidVerificationCodeException.class,
        () -> service.execute(EMAIL, CODE));

    verify(token, never()).incrementAttempts();
    verify(userTokenRepository, never()).save(any());
  }
}