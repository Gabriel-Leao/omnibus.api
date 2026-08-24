package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;

/**
 * Output port for sending account-registration-related emails to customers.
 */
public interface OtpSenderPort {

  /**
   * Sends an OTP to a customer.
   *
   * @param customer the customer receiving the OTP
   * @param code     the plain-text OTP
   * @param OtpType  the purpose of the OTP
   */
  void sendOtp(Customer customer, String code, OtpType OtpType);

  /**
   * Notifies an existing account holder that someone attempted to register with their email.
   *
   * @param email the email address that was already registered
   */
  void sendDuplicateRegistrationNotice(String email);
}
