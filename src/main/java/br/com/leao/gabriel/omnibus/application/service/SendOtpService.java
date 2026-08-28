package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.application.usecase.SendOtpUseCase;
import br.com.leao.gabriel.omnibus.domain.exception.ResendCooldownActiveException;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpSenderPort;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles requests to send verification OTPs to customers.
 */
@Service
@RequiredArgsConstructor
public class SendOtpService implements SendOtpUseCase {

  private final CustomerRepositoryPort customerRepositoryPort;
  private final VerificationOtpIssuer verificationOtpIssuer;
  private final OtpSenderPort otpSender;
  private final UserTokenRepositoryPort userTokenRepository;

  /**
   * Sends a verification OTP when the customer is eligible and the resend cooldown has elapsed.
   *
   * @param email the customer's email address
   *
   * @param otpType the purpose of the OTP
   */
  @Override
  @Transactional
  public void execute(String email, OtpType otpType) {
    var customerOptional = customerRepositoryPort.findByEmail(email);

    if (customerOptional.isEmpty()) {
      return;
    }

    var customer = customerOptional.get();

    if (isNotEligible(customer.isActivated(), otpType)) {
      return;
    }

    var token =
        userTokenRepository.findLatestByUserIdAndType(customer.getId(), otpType).orElse(null);

    if (token != null && !token.isResendAllowed()) {
      throw new ResendCooldownActiveException();
    }

    var otp = verificationOtpIssuer.issue(customer.getId(), otpType);

    otpSender.sendOtp(customer, otp, otpType);
  }

  private boolean isNotEligible(boolean activated, OtpType otpType) {
    return switch (otpType) {
      case ACCOUNT_ACTIVATION -> activated;
      case PASSWORD_RESET, EMAIL_CHANGE -> !activated;
    };
  }
}
