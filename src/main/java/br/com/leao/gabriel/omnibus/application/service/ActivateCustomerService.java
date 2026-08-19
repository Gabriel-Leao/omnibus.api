package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.domain.exception.VerificationAttemptsExceededException;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.TokenType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import br.com.leao.gabriel.omnibus.domain.port.in.ActivateCustomerUseCase;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpGeneratorPort;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Activates a customer account using a one-time code sent by email. Enforces a maximum number of
 * verification attempts before the code must be regenerated via a resend request.
 */
@Service
@RequiredArgsConstructor
public class ActivateCustomerService implements ActivateCustomerUseCase {

  private final CustomerRepositoryPort customerRepository;
  private final UserTokenRepositoryPort userTokenRepository;
  private final OtpGeneratorPort otpGenerator;
  private final TokenIssuerPort tokenIssuer;
  private final PrincipalFactory principalFactory;

  @Override
  public String execute(String email, String submittedCode) {
    Customer customer =
        customerRepository.findByEmail(email).orElseThrow(InvalidVerificationCodeException::new);

    UserToken token =
        userTokenRepository
            .findLatestByUserIdAndType(customer.getId(), TokenType.ACCOUNT_ACTIVATION)
            .orElseThrow(InvalidVerificationCodeException::new);

    if (!token.isActive()) {
      throw token.isAttemptsExceeded()
          ? new VerificationAttemptsExceededException()
          : new InvalidVerificationCodeException();
    }

    boolean codeMatches = otpGenerator.hash(submittedCode).equals(token.getCodeHash());
    if (!codeMatches) {
      userTokenRepository.save(token.incrementAttempts());
      throw new InvalidVerificationCodeException();
    }

    var activatedCustomer = customerRepository.save(customer.activate());
    userTokenRepository.save(token.markUsed());

    return tokenIssuer.issueAccessToken(principalFactory.forCustomer(activatedCustomer));
  }
}
