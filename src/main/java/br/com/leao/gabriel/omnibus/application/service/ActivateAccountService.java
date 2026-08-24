package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.application.factory.AuthenticatedPrincipalFactory;
import br.com.leao.gabriel.omnibus.application.usecase.ActivateAccountUseCase;
import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivateAccountService
    implements ActivateAccountUseCase {

  private final OtpVerifier otpVerifier;
  private final CustomerRepositoryPort customerRepository;
  private final TokenIssuerPort tokenIssuer;
  private final AuthenticatedPrincipalFactory principalFactory;

  @Override
  @Transactional(noRollbackFor = InvalidVerificationCodeException.class)
  public String execute(String email, String code) {

    var customer =
        otpVerifier.verify(
            email,
            code,
            OtpType.ACCOUNT_ACTIVATION
        );

    var activatedCustomer =
        customerRepository.save(customer.activate());

    return tokenIssuer.issueAccessToken(
        principalFactory.forCustomer(activatedCustomer)
    );
  }
}