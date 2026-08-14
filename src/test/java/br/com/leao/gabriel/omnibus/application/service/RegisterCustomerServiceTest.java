package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.exception.EmailAlreadyRegisteredException;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.PasswordEncoderPort;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerServiceTest {

  @Mock private PasswordEncoderPort passwordEncoder;

  @Mock private CustomerRepositoryPort customerRepository;

  @InjectMocks private RegisterCustomerService service;

  @Test
  void shouldRegisterCustomerSuccessfully() {
    var name = "Gabriel";
    var email = "gabriel@email.com";
    var rawPassword = "password123";
    var hashedPassword = "hashed-password";
    var birthDate = LocalDate.of(2000, 1, 1);
    var photoUrl = "https://example.com/photo.jpg";

    var savedCustomer = Customer.register(name, email, hashedPassword, birthDate, photoUrl);

    when(customerRepository.existsByEmail(email)).thenReturn(false);

    when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);

    when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

    var result = service.execute(name, email, rawPassword, birthDate, photoUrl);

    assertThat(result).isSameAs(savedCustomer);

    verify(customerRepository).existsByEmail(email);
    verify(passwordEncoder).encode(rawPassword);
    verify(customerRepository).save(any(Customer.class));
  }

  @Test
  void shouldNotRegisterCustomerWhenEmailIsAlreadyRegistered() {
    var email = "gabriel@email.com";

    when(customerRepository.existsByEmail(email)).thenReturn(true);

    assertThatThrownBy(
            () -> service.execute("Gabriel", email, "password123", LocalDate.of(2000, 1, 1), null))
        .isInstanceOf(EmailAlreadyRegisteredException.class);

    verify(customerRepository).existsByEmail(email);
    verifyNoInteractions(passwordEncoder);
    verify(customerRepository, never()).save(any());
  }
}
