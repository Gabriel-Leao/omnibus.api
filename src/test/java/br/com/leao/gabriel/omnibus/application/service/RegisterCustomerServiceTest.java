package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.port.out.ActivationCodeSenderPort;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.PasswordEncoderPort;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RegisterCustomerService}, exercised in complete isolation from Spring — no
 * application context, no database — which is the practical benefit of keeping the domain and
 * application layers free of framework dependencies.
 */
@ExtendWith(MockitoExtension.class)
class RegisterCustomerServiceTest {

  private static final String NAME = "Gabriel Leão";
  private static final String EMAIL = "gabriel@teste.com";
  private static final String RAW_PASSWORD = "senha1234";
  private static final String HASHED_PASSWORD = "hashed-password";
  private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 1);

  @Mock
  private CustomerRepositoryPort customerRepository;
  @Mock
  private PasswordEncoderPort passwordEncoder;
  @Mock
  private ActivationCodeSenderPort activationCodeSender;

  private RegisterCustomerService service;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    service =
        new RegisterCustomerService(customerRepository, passwordEncoder, activationCodeSender);
  }

  @Test
  @DisplayName("Should hash the password, persist the customer and send an activation code")
  void shouldRegisterNewCustomerSuccessfully() {
    when(customerRepository.existsByEmail(EMAIL)).thenReturn(false);
    when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
    when(customerRepository.save(any(Customer.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    service.execute(NAME, EMAIL, RAW_PASSWORD, BIRTH_DATE, null);

    ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository).save(customerCaptor.capture());

    Customer persistedCustomer = customerCaptor.getValue();
    assertThat(persistedCustomer.getName()).isEqualTo(NAME);
    assertThat(persistedCustomer.getEmail()).isEqualTo(EMAIL);
    assertThat(persistedCustomer.getPasswordHash()).isEqualTo(HASHED_PASSWORD);

    verify(activationCodeSender).sendActivationCode(persistedCustomer);
    verify(activationCodeSender, never()).sendDuplicateRegistrationNotice(anyString());
  }

  @Test
  @DisplayName("Should not persist and should send a duplicate notice when the email is already taken")
  void shouldNotifyDuplicateRegistrationWithoutPersisting() {
    when(customerRepository.existsByEmail(EMAIL)).thenReturn(true);

    service.execute(NAME, EMAIL, RAW_PASSWORD, BIRTH_DATE, null);

    verify(customerRepository, never()).save(any(Customer.class));
    verify(passwordEncoder, never()).encode(anyString());
    verify(activationCodeSender).sendDuplicateRegistrationNotice(EMAIL);
    verify(activationCodeSender, never()).sendActivationCode(any(Customer.class));
  }

  @Test
  @DisplayName("Should complete without throwing regardless of the email already existing")
  void shouldNeverThrowRegardlessOfOutcome() {
    when(customerRepository.existsByEmail(EMAIL)).thenReturn(true);

    org.junit.jupiter.api.Assertions.assertDoesNotThrow(
        () -> service.execute(NAME, EMAIL, RAW_PASSWORD, BIRTH_DATE, null));
  }
}