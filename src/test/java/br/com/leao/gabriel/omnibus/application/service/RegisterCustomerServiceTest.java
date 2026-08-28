package br.com.leao.gabriel.omnibus.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpSenderPort;
import br.com.leao.gabriel.omnibus.domain.port.out.PasswordEncoderPort;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerServiceTest {

  private static final String NAME = "Gabriel Leão";
  private static final String EMAIL = "gabriel@example.com";
  private static final String RAW_PASSWORD = "password123";
  private static final String PASSWORD_HASH = "hashed-password";
  private static final String CUSTOMER_ID = "customer-id";
  private static final String GENERATED_CODE = "482913";
  private static final LocalDate BIRTH_DATE = LocalDate.of(2003, 8, 20);
  private static final String PHOTO_URL = "https://example.com/photo.jpg";

  @Mock private CustomerRepositoryPort customerRepository;
  @Mock private PasswordEncoderPort passwordEncoder;
  @Mock private OtpSenderPort otpSender;
  @Mock private VerificationOtpIssuer verificationOtpIssuer;
  @Mock private Customer savedCustomer;

  private RegisterCustomerService service;

  @BeforeEach
  void setUp() {
    service =
        new RegisterCustomerService(
            customerRepository, passwordEncoder, otpSender, verificationOtpIssuer);
  }

  @Test
  @DisplayName("Should register a new customer and send an activation OTP")
  void shouldRegisterNewCustomer() {
    when(customerRepository.existsByEmail(EMAIL)).thenReturn(false);
    when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(PASSWORD_HASH);
    when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
    when(savedCustomer.getId()).thenReturn(CUSTOMER_ID);
    when(verificationOtpIssuer.issue(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION))
        .thenReturn(GENERATED_CODE);

    service.execute(NAME, EMAIL, RAW_PASSWORD, BIRTH_DATE, PHOTO_URL);

    verify(customerRepository).save(any(Customer.class));
    verify(passwordEncoder).encode(RAW_PASSWORD);
    verify(verificationOtpIssuer).issue(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION);
    verify(otpSender).sendOtp(savedCustomer, GENERATED_CODE, OtpType.ACCOUNT_ACTIVATION);
  }

  @Test
  @DisplayName("Should notify an existing account without creating another customer")
  void shouldHandleDuplicateEmail() {
    when(customerRepository.existsByEmail(EMAIL)).thenReturn(true);

    service.execute(NAME, EMAIL, RAW_PASSWORD, BIRTH_DATE, PHOTO_URL);

    verify(otpSender).sendDuplicateRegistrationNotice(EMAIL);
    verify(customerRepository, never()).save(any(Customer.class));
    verify(passwordEncoder, never()).encode(anyString());
    verifyNoInteractions(verificationOtpIssuer);
  }
}
