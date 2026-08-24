package br.com.leao.gabriel.omnibus.application.usecase;

import br.com.leao.gabriel.omnibus.domain.model.OtpType;

/**
 * Defines the use case for resending a verification code.
 */
public interface SendOtpUseCase {

  /**
   * Resends a verification code to the specified email address.
   *
   * @param email   the customer's email address
   * @param otpType the type of verification code to resend
   */
  void execute(String email, OtpType otpType);
}