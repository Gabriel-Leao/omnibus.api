package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.port.in.RegisterCustomerUseCase;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.PasswordEncoderPort;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterCustomerService implements RegisterCustomerUseCase {

  private final PasswordEncoderPort passwordEncoder;
  private final CustomerRepositoryPort customerRepository;

  @Override
  public Customer execute(String name, String email, String rawPassword, LocalDate birthDate,
      String photoUrl) {
    var isEmailTaken = customerRepository.existsByEmail(email);
    if (isEmailTaken) {
      throw new RuntimeException("Email already exists");
    }
    var hashedPassword = passwordEncoder.encode(rawPassword);
    var customer = Customer.register(name, email, hashedPassword, birthDate, photoUrl);
    return customerRepository.save(customer);
  }
}
