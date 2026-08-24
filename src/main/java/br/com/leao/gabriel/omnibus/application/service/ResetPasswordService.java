package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.application.usecase.ResetPasswordUseCase;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPasswordUseCase {

  private final CustomerRepositoryPort customerRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * @param userId
   * @param newPassword
   */
  @Override
  @Transactional
  public void execute(UUID userId, String newPassword) {
    var customer =
        customerRepository
            .findById(userId)
            .orElseThrow(/* exceção */);

    var passwordHash =
        passwordEncoder.encode(newPassword);

    customerRepository.save(
        customer.changePassword(passwordHash));
  }
}
