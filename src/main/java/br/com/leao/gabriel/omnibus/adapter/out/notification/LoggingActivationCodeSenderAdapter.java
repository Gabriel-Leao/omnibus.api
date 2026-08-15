package br.com.leao.gabriel.omnibus.adapter.out.notification;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.port.out.ActivationCodeSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Temporary placeholder adapter that logs instead of sending real emails, until the email
 * infrastructure (activation code generation and delivery) is implemented.
 */
@Component
public class LoggingActivationCodeSenderAdapter implements ActivationCodeSenderPort {

  private static final Logger log = LoggerFactory.getLogger(
      LoggingActivationCodeSenderAdapter.class);

  @Override
  public void sendActivationCode(Customer customer) {
    log.info("Activation code would be sent to {}", customer.getEmail());
  }

  @Override
  public void sendDuplicateRegistrationNotice(String email) {
    log.info("Duplicate registration notice would be sent to {}", email);
  }
}