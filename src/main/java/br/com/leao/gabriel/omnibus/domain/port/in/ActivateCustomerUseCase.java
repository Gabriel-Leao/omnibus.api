package br.com.leao.gabriel.omnibus.domain.port.in;

import br.com.leao.gabriel.omnibus.domain.exception.InvalidVerificationCodeException;
import br.com.leao.gabriel.omnibus.domain.exception.VerificationAttemptsExceededException;

/**
 * Use case for activating a customer account using a one-time verification code.
 */
public interface ActivateCustomerUseCase {

  /**
   * Activates a customer account using the submitted verification code.
   *
   * <p>The code is validated against the customer's latest account-activation token. When the code
   * is valid, the customer account is activated, the verification token is marked as used, and an
   * access token is issued for the activated customer.
   *
   * @param email the email address of the customer to activate
   * @param code  the plain-text account-activation code submitted by the customer
   * @return an access token for the newly activated customer
   * @throws InvalidVerificationCodeException      if the customer does not exist, no activation
   *                                               token is available, the token is inactive,
   *                                               expired, already used, or the submitted code is
   *                                               invalid
   * @throws VerificationAttemptsExceededException if the maximum number of verification attempts
   *                                               has been exceeded and a new activation code must
   *                                               be requested
   */
  String execute(String email, String code);
}
