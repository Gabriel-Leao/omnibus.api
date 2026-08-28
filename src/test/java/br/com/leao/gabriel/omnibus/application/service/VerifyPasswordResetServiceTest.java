package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerifyPasswordResetServiceTest {

  private static final String EMAIL = "gabriel@example.com";
  private static final String CODE = "482913";
  private static final String CUSTOMER_ID = "customer-id";
  private static final String RESET_TOKEN = "reset-token";

  @Mock private OtpVerifier otpVerifier;
  @Mock private TokenIssuerPort tokenIssuer;
  @Mock private Customer customer;

  private VerifyPasswordResetService service;

  @BeforeEach
  void setUp() {
    service = new VerifyPasswordResetService(otpVerifier, tokenIssuer);
  }

  @Test
  @DisplayName("Should issue a password reset token after successful OTP verification")
  void shouldIssuePasswordResetToken() {
    when(otpVerifier.verify(EMAIL, CODE, OtpType.PASSWORD_RESET)).thenReturn(customer);
    when(customer.getId()).thenReturn(CUSTOMER_ID);
    when(tokenIssuer.issuePasswordResetToken(CUSTOMER_ID)).thenReturn(RESET_TOKEN);

    String result = service.execute(EMAIL, CODE);

    assertThat(result).isEqualTo(RESET_TOKEN);
    verify(tokenIssuer).issuePasswordResetToken(CUSTOMER_ID);
  }

  @Test
  @DisplayName("Should propagate an invalid verification code")
  void shouldPropagateInvalidCode() {
    when(otpVerifier.verify(EMAIL, CODE, OtpType.PASSWORD_RESET))
        .thenThrow(new InvalidVerificationCodeException());

    assertThrows(InvalidVerificationCodeException.class, () -> service.execute(EMAIL, CODE));
  }
}
