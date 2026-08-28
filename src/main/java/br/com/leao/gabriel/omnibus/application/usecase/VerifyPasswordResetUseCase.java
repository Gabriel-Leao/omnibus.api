package br.com.leao.gabriel.omnibus.application.usecase;

/**
 * Use case for verifying a password reset code.
 */
public interface VerifyPasswordResetUseCase {

  /**
   * Verifies a password reset code and returns a reset token.
   *
   * @param email the customer's email address
   *
   * @param code the submitted verification code
   *
   * @return a password reset token
   */
  String execute(String email, String code);
}
