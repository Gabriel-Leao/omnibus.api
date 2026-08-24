package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerServiceTest {

  private static final String NAME = "Gabriel Leão";
  private static final String EMAIL = "gabriel@teste.com";
  private static final String RAW_PASSWORD = "senha1234";
  private static final String HASHED_PASSWORD = "hashed-password";
  private static final String CUSTOMER_ID = "customer-id";
  private static final String GENERATED_CODE = "482913";
  private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 1);
  private static final String PHOTO_URL = "https://example.com/photo.jpg";

  @Mock
  private CustomerRepositoryPort customerRepository;

  @Mock
  private PasswordEncoderPort passwordEncoder;

  @Mock
  private OtpSenderPort activationCodeSender;

  @Mock
  private VerificationOtpIssuer verificationOtpIssuer;

  @Mock
  private Customer savedCustomer;

  private RegisterCustomerService service;

  @BeforeEach
  void setUp() {
    service =
        new RegisterCustomerService(
            customerRepository, passwordEncoder, activationCodeSender, verificationOtpIssuer);
  }

  @Test
  @DisplayName("Should register new customer")
  void shouldRegisterNewCustomer() {
    when(customerRepository.existsByEmail(EMAIL)).thenReturn(false);
    when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);

    when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

    when(savedCustomer.getId()).thenReturn(CUSTOMER_ID);

    when(verificationOtpIssuer.issue(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION))
        .thenReturn(GENERATED_CODE);

    service.execute(NAME, EMAIL, RAW_PASSWORD, BIRTH_DATE, PHOTO_URL);

    ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);

    verify(customerRepository).save(captor.capture());

    Customer customer = captor.getValue();

    assertThat(customer.getName()).isEqualTo(NAME);
    assertThat(customer.getEmail()).isEqualTo(EMAIL);
    assertThat(customer.getPasswordHash()).isEqualTo(HASHED_PASSWORD);
    assertThat(customer.getBirthDate()).isEqualTo(BIRTH_DATE);
    assertThat(customer.getPhotoUrl()).isEqualTo(PHOTO_URL);

    verify(passwordEncoder).encode(RAW_PASSWORD);

    verify(verificationOtpIssuer).issue(CUSTOMER_ID, OtpType.ACCOUNT_ACTIVATION);

    verify(activationCodeSender).sendActivationCode(savedCustomer, GENERATED_CODE);

    verify(activationCodeSender, never()).sendDuplicateRegistrationNotice(anyString());
  }

  @Test
  @DisplayName("Should send duplicate notice without creating customer")
  void shouldHandleDuplicateEmail() {
    when(customerRepository.existsByEmail(EMAIL)).thenReturn(true);

    service.execute(NAME, EMAIL, RAW_PASSWORD, BIRTH_DATE, PHOTO_URL);

    verify(activationCodeSender).sendDuplicateRegistrationNotice(EMAIL);

    verify(customerRepository, never()).save(any(Customer.class));

    verify(passwordEncoder, never()).encode(anyString());

    verifyNoInteractions(verificationOtpIssuer);

    verify(activationCodeSender, never()).sendActivationCode(any(), anyString());
  }

  @Test
  @DisplayName("Should not throw for duplicate email")
  void shouldNotThrowForDuplicateEmail() {
    when(customerRepository.existsByEmail(EMAIL)).thenReturn(true);

    assertDoesNotThrow(() -> service.execute(NAME, EMAIL, RAW_PASSWORD, BIRTH_DATE, PHOTO_URL));
  }
}
