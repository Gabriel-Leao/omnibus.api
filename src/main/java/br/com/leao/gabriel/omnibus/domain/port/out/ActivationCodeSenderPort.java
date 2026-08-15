package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.Customer;

/**
 * Output port for notifying a customer about account registration events by email.
 *
 * <p>The two notice types are intentionally symmetric: both are triggered from the same
 * registration flow, regardless of whether the email address already had an account, so that no
 * information about account existence is observable from the HTTP response alone.
 */
public interface ActivationCodeSenderPort {

  /**
   * Sends an activation code to a newly registered customer.
   */
  void sendActivationCode(Customer customer);

  /**
   * Notifies an existing account holder that someone attempted to register with their email.
   */
  void sendDuplicateRegistrationNotice(String email);
}