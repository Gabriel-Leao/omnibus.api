package br.com.leao.gabriel.omnibus.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.exception.CustomerNotFoundException;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpSenderPort;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

  private static final UUID CUSTOMER_ID = UUID.randomUUID();
  private static final String NEW_PASSWORD = "new-password";
  private static final String PASSWORD_HASH = "hashed-password";

  @Mock private CustomerRepositoryPort customerRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private OtpSenderPort otpSender;
  @Mock private Customer customer;
  @Mock private Customer updatedCustomer;

  private ResetPasswordService service;

  @BeforeEach
  void setUp() {
    service = new ResetPasswordService(customerRepository, passwordEncoder, otpSender);
  }

  @Test
  @DisplayName("Should hash the new password and save the updated customer")
  void shouldResetPassword() {
    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
    when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(PASSWORD_HASH);
    when(customer.changePassword(PASSWORD_HASH)).thenReturn(updatedCustomer);

    service.execute(CUSTOMER_ID, NEW_PASSWORD);

    verify(passwordEncoder).encode(NEW_PASSWORD);
    verify(customer).changePassword(PASSWORD_HASH);
    verify(customerRepository).save(updatedCustomer);
  }

  @Test
  @DisplayName("Should reject a reset when the customer does not exist")
  void shouldRejectUnknownCustomer() {
    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Optional.empty());

    assertThrows(CustomerNotFoundException.class, () -> service.execute(CUSTOMER_ID, NEW_PASSWORD));

    verify(passwordEncoder, never()).encode(any());
    verify(customerRepository, never()).save(any());
  }
}
