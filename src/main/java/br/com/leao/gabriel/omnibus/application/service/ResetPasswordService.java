package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.application.usecase.ResetPasswordUseCase;
import br.com.leao.gabriel.omnibus.domain.exception.CustomerNotFoundException;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpSenderPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
  private final OtpSenderPort otpSender;

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

    otpSender.sendPasswordResetNotice(customer.getEmail());
  }
}
