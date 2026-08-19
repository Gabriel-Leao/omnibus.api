package br.com.leao.gabriel.omnibus.adapter.out.notification;

import br.com.leao.gabriel.omnibus.domain.exception.RegistrationEmailDeliveryException;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.port.out.ActivationCodeSenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends account-registration-related emails using SMTP via {@link JavaMailSender}.
 */
@Component
@RequiredArgsConstructor
public class SmtpActivationCodeSenderAdapter implements ActivationCodeSenderPort {

  private final JavaMailSender mailSender;

  @Value("${email.sender-address}")
  private String senderAddress;

  @Override
  public void sendActivationCode(Customer customer, String code) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(senderAddress);
      message.setTo(customer.getEmail());
      message.setSubject("Confirme sua conta Omnibus");
      message.setText(
          "Olá, "
              + customer.getName()
              + "!\n\n"
              + "Seu código de ativação é: "
              + code
              + "\n\n"
              + "Esse código expira em 15 minutos.");
      mailSender.send(message);
    } catch (MailException e) {
      throw new RegistrationEmailDeliveryException(e);
    }
  }

  @Override
  public void sendDuplicateRegistrationNotice(String email) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(senderAddress);
    message.setTo(email);
    message.setSubject("Tentativa de cadastro na Omnibus");
    message.setText(
        "Alguém tentou criar uma conta com este e-mail, mas você já possui uma conta na "
            + "Omnibus. Se foi você, faça login normalmente. Se não foi você, ignore este "
            + "e-mail.");
    mailSender.send(message);
  }
}
