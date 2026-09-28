package br.com.leao.gabriel.omnibus.identity.application.service;

import br.com.leao.gabriel.omnibus.identity.application.usecase.RegisterCustomerUseCase;
import br.com.leao.gabriel.omnibus.identity.domain.model.Customer;
import br.com.leao.gabriel.omnibus.identity.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.identity.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.identity.domain.port.out.PasswordEncoderPort;
import br.com.leao.gabriel.omnibus.identity.event.DuplicateRegistrationAttemptedEvent;
import br.com.leao.gabriel.omnibus.identity.event.OtpIssuedEvent;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  private final ApplicationEventPublisher eventPublisher;
  private final VerificationOtpIssuer verificationOtpIssuer;

  /**
   * Handles the execute operation.
   */
  @Override
  @Transactional
  public void execute(
      String name, String email, String rawPassword, LocalDate birthDate, String photoUrl) {
    if (customerRepository.existsByEmail(email)) {
      eventPublisher.publishEvent(new DuplicateRegistrationAttemptedEvent(email));
      return;
    }

    var passwordHash = passwordEncoder.encode(rawPassword);
    Customer customer = Customer.register(name, email, passwordHash, birthDate, photoUrl);
    Customer savedCustomer = customerRepository.save(customer);

    String code = verificationOtpIssuer.issue(savedCustomer.getId(), OtpType.ACCOUNT_ACTIVATION);
    eventPublisher.publishEvent(
        new OtpIssuedEvent(
            savedCustomer.getEmail(),
            savedCustomer.getName(),
            code,
            OtpType.ACCOUNT_ACTIVATION.getEmailSubject(),
            OtpType.ACCOUNT_ACTIVATION.getEmailTag()));
  }
}
