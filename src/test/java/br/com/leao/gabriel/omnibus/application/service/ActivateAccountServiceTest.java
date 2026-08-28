package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.application.factory.AuthenticatedPrincipalFactory;
import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivateAccountServiceTest {

  private static final String EMAIL = "gabriel@example.com";
  private static final String CODE = "482913";
  private static final String ACCESS_TOKEN = "access-token";

  @Mock private OtpVerifier otpVerifier;
  @Mock private CustomerRepositoryPort customerRepository;
  @Mock private TokenIssuerPort tokenIssuer;
  @Mock private AuthenticatedPrincipalFactory principalFactory;
  @Mock private Customer customer;
  @Mock private Customer activatedCustomer;
  @Mock private AuthenticatedPrincipal principal;

  private ActivateAccountService service;

  @BeforeEach
  void setUp() {
    service =
        new ActivateAccountService(otpVerifier, customerRepository, tokenIssuer, principalFactory);
  }

  @Test
  @DisplayName("Should activate the customer and issue an access token")
  void shouldActivateAccount() {
    when(otpVerifier.verify(EMAIL, CODE, OtpType.ACCOUNT_ACTIVATION)).thenReturn(customer);
    when(customer.activate()).thenReturn(activatedCustomer);
    when(customerRepository.save(activatedCustomer)).thenReturn(activatedCustomer);
    when(principalFactory.forCustomer(activatedCustomer)).thenReturn(principal);
    when(tokenIssuer.issueAccessToken(principal)).thenReturn(ACCESS_TOKEN);

    String result = service.execute(EMAIL, CODE);

    assertThat(result).isEqualTo(ACCESS_TOKEN);
    verify(customerRepository).save(activatedCustomer);
    verify(principalFactory).forCustomer(activatedCustomer);
    verify(tokenIssuer).issueAccessToken(principal);
  }

  @Test
  @DisplayName("Should propagate an invalid verification code")
  void shouldPropagateInvalidCode() {
    when(otpVerifier.verify(EMAIL, CODE, OtpType.ACCOUNT_ACTIVATION))
        .thenThrow(new InvalidVerificationCodeException());

    assertThrows(InvalidVerificationCodeException.class, () -> service.execute(EMAIL, CODE));

    verifyNoInteractions(customerRepository, tokenIssuer, principalFactory);
  }
}
