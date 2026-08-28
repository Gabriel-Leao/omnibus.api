package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.application.usecase.VerifyPasswordResetUseCase;
import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles verification of password reset OTPs and issuance of reset tokens.
 */
@Service
@RequiredArgsConstructor
public class VerifyPasswordResetService implements VerifyPasswordResetUseCase {

  private final OtpVerifier otpVerifier;
  private final TokenIssuerPort tokenIssuer;

  /**
   * Verifies a password reset OTP and issues a password reset token.
   *
   * @param email the customer's email address
   *
   * @param code the submitted verification code
   *
   * @return a password reset token
   */
  @Override
  @Transactional(noRollbackFor = InvalidVerificationCodeException.class)
  public String execute(String email, String code) {

    var customer = otpVerifier.verify(email, code, OtpType.PASSWORD_RESET);

    return tokenIssuer.issuePasswordResetToken(customer.getId());
  }
}
