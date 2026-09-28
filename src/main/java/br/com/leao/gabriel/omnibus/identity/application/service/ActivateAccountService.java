package br.com.leao.gabriel.omnibus.identity.application.service;

import br.com.leao.gabriel.omnibus.identity.application.factory.AuthenticatedPrincipalFactory;
import br.com.leao.gabriel.omnibus.identity.application.usecase.ActivateAccountUseCase;
import br.com.leao.gabriel.omnibus.identity.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.identity.domain.exception.VerificationAttemptsExceededException;
import br.com.leao.gabriel.omnibus.identity.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.identity.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.identity.domain.port.out.TokenIssuerPort;
import br.com.leao.gabriel.omnibus.identity.event.CustomerRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles customer account activation and access token issuance.
 */
@Service
@RequiredArgsConstructor
public class ActivateAccountService implements ActivateAccountUseCase {

  private final OtpVerifier otpVerifier;
  private final CustomerRepositoryPort customerRepository;
  private final TokenIssuerPort tokenIssuer;
  private final AuthenticatedPrincipalFactory principalFactory;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Activates a customer account and issues an access token.
   *
   * @param email the customer's email address
   * @param code  the submitted activation code
   * @return a signed access token
   */
  @Override
  @Transactional(
      noRollbackFor = {
        InvalidVerificationCodeException.class,
        VerificationAttemptsExceededException.class
      })
  public String execute(String email, String code) {

    var customer = otpVerifier.verify(email, code, OtpType.ACCOUNT_ACTIVATION);
    var activatedCustomer = customerRepository.save(customer.activate());
    eventPublisher.publishEvent(
        new CustomerRegisteredEvent(activatedCustomer.getEmail(), activatedCustomer.getName()));

    return tokenIssuer.issueAccessToken(principalFactory.forCustomer(activatedCustomer));
  }
}
