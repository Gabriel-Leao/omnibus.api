package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.application.usecase.VerifyPasswordResetUseCase;
import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyPasswordResetService
    implements VerifyPasswordResetUseCase {

  private final OtpVerifier otpVerifier;
  private final TokenIssuerPort tokenIssuer;

  @Override
  @Transactional(noRollbackFor = InvalidVerificationCodeException.class)
  public String execute(String email, String code) {

    var customer =
        otpVerifier.verify(
            email,
            code,
            OtpType.PASSWORD_RESET
        );

    return tokenIssuer.issuePasswordResetToken(customer.getId());
  }
}