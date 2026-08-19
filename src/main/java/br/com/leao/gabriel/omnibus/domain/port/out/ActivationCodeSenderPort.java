package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.Customer;

/**
 * Output port for sending account-registration-related emails to customers.
 */
public interface ActivationCodeSenderPort {

  /**
   * Sends an activation code to a newly registered customer.
   *
   * @param customer the customer who just registered
   * @param code     the plain-text activation code
   */
  void sendActivationCode(Customer customer, String code);

  /**
   * Notifies an existing account holder that someone attempted to register with their email.
   *
   * @param email the email address that was already registered
   */
  void sendDuplicateRegistrationNotice(String email);
}
