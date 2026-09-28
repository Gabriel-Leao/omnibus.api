package br.com.leao.gabriel.omnibus.identity.application.service;

import br.com.leao.gabriel.omnibus.identity.application.usecase.ResetPasswordUseCase;
import br.com.leao.gabriel.omnibus.identity.domain.exception.CustomerNotFoundException;
import br.com.leao.gabriel.omnibus.identity.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.identity.event.PasswordResetCompletedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles password reset operations after a valid reset token has been verified.
 */
@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

  private final CustomerRepositoryPort customerRepository;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Changes the customer password after a valid password reset request.
   *
   * @param userId      the customer's identifier
   * @param newPassword the new plain-text password
   */
  @Override
  @Transactional
  public void execute(UUID userId, String newPassword) {
    var customer =
        customerRepository
            .findById(userId)
            .orElseThrow(() -> new CustomerNotFoundException(userId));

    var passwordHash = passwordEncoder.encode(newPassword);
    customerRepository.save(customer.changePassword(passwordHash));

    eventPublisher.publishEvent(new PasswordResetCompletedEvent(customer.getEmail()));
  }
}
