package br.com.leao.gabriel.omnibus.adapter.out.notification;

import br.com.leao.gabriel.omnibus.domain.exception.RegistrationEmailDeliveryException;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpSenderPort;
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
public class SmtpOtpSenderAdapter implements OtpSenderPort {

  private final JavaMailSender mailSender;

  @Value("${email.sender-address}")
  private String senderAddress;

  /**
   * @param customer the customer receiving the OTP
   * @param code     the plain-text OTP
   * @param OtpType  the purpose of the OTP
   */
  @Override
  public void sendOtp(Customer customer, String code, OtpType OtpType) {
    String subject = OtpType.getEmailSubject();
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(senderAddress);
      message.setTo(customer.getEmail());
      message.setSubject(subject);
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
