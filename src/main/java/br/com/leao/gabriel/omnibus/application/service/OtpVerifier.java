package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.domain.exception.VerificationAttemptsExceededException;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpGeneratorPort;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Verifies OTP codes and marks valid tokens as used.
 */
@Service
@RequiredArgsConstructor
public class OtpVerifier {

  private final CustomerRepositoryPort customerRepository;
  private final UserTokenRepositoryPort userTokenRepository;
  private final OtpGeneratorPort otpGenerator;

  /**
   * Handles the verify operation.
   */
  public Customer verify(String email, String submittedCode, OtpType otpType) {

    Customer customer =
        customerRepository.findByEmail(email).orElseThrow(InvalidVerificationCodeException::new);

    if (!customer.canUseOtp(otpType)) {
      throw new InvalidVerificationCodeException();
    }

    UserToken token =
        userTokenRepository
            .findLatestByUserIdAndType(customer.getId(), otpType)
            .orElseThrow(InvalidVerificationCodeException::new);

    if (!token.isUsable()) {
      throw token.isAttemptsExceeded()
          ? new VerificationAttemptsExceededException()
          : new InvalidVerificationCodeException();
    }

    boolean matches = otpGenerator.hash(submittedCode).equals(token.getCodeHash());

    if (!matches) {
      var updatedToken = token.registerFailedAttempt();
      userTokenRepository.save(updatedToken);

      throw updatedToken.isAttemptsExceeded()
          ? new VerificationAttemptsExceededException()
          : new InvalidVerificationCodeException();
    }

    userTokenRepository.save(token.markUsed());

    return customer;
  }
}
