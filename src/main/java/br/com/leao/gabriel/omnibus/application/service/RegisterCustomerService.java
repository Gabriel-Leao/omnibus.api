package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.TokenType;
import br.com.leao.gabriel.omnibus.domain.port.in.RegisterCustomerUseCase;
import br.com.leao.gabriel.omnibus.domain.port.out.ActivationCodeSenderPort;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.PasswordEncoderPort;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Handles customer registration. To avoid user enumeration, this service always completes
 * successfully from the caller's perspective, regardless of whether the email was already
 * registered — the actual outcome is only communicated via email.
 */
@Service
@RequiredArgsConstructor
public class RegisterCustomerService implements RegisterCustomerUseCase {

  private final CustomerRepositoryPort customerRepository;
  private final PasswordEncoderPort passwordEncoder;
  private final ActivationCodeSenderPort activationCodeSender;
  private final VerificationCodeIssuer verificationCodeIssuer;

  @Override
  @Transactional
  public void execute(
      String name, String email, String rawPassword, LocalDate birthDate, String photoUrl) {
    if (customerRepository.existsByEmail(email)) {
      activationCodeSender.sendDuplicateRegistrationNotice(email);
      return;
    }

    var passwordHash = passwordEncoder.encode(rawPassword);
    Customer customer = Customer.register(name, email, passwordHash, birthDate, photoUrl);
    Customer savedCustomer = customerRepository.save(customer);

    String code = verificationCodeIssuer.issue(savedCustomer.getId(), TokenType.ACCOUNT_ACTIVATION);
    activationCodeSender.sendActivationCode(savedCustomer, code);
  }
}