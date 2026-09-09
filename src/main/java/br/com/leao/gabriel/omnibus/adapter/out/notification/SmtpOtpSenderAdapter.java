package br.com.leao.gabriel.omnibus.adapter.out.notification;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.OtpSenderPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Sends account-registration-related emails using SMTP via {@link JavaMailSender}.
 *
 * <p>Every method here is {@link Async @Async}: callers (see {@code SendOtpService},
 * {@code RegisterCustomerService}, {@code ResetPasswordService}) sit behind endpoints that must
 * respond in the same amount of time regardless of whether an email actually gets sent, so that
 * response time itself doesn't reveal whether an email address is registered. A failed send is
 * therefore only logged here, never thrown back to an HTTP caller that has already received its
 * response by the time this runs.
 */
@Component
@RequiredArgsConstructor
public class SmtpOtpSenderAdapter implements OtpSenderPort {

  private static final Logger log = LoggerFactory.getLogger(SmtpOtpSenderAdapter.class);

  private final JavaMailSender mailSender;

  @Value("${spring.mail.from}")
  private String senderAddress;

  /**
   * Sends an OTP to the specified customer.
   *
   * @param customer the customer receiving the OTP
   * @param code     the plain-text OTP
   * @param otpType  the purpose of the OTP
   */
  @Override
  @Async
  public void sendOtp(Customer customer, String code, OtpType otpType) {
    String subject = otpType.getEmailSubject();
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
      log.error("Failed to send {} OTP email to customer {}", otpType, customer.getId(), e);
    }
  }

  /**
   * Sends a notice when an already registered email is used for registration.
   *
   * @param email the email address that was already registered
   */
  @Override
  @Async
  public void sendDuplicateRegistrationNotice(String email) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(senderAddress);
      message.setTo(email);
      message.setSubject("Tentativa de cadastro na Omnibus");
      message.setText(
          "Alguém tentou criar uma conta com este e-mail, mas você já possui uma conta na "
              + "Omnibus. Se foi você, faça login normalmente. Se não foi você, ignore este "
              + "e-mail.");
      mailSender.send(message);
    } catch (MailException e) {
      log.error("Failed to send duplicate-registration notice to {}", email, e);
    }
  }

  @Override
  @Async
  public void sendPasswordResetNotice(String email) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(senderAddress);
      message.setTo(email);
      message.setSubject("Sua senha foi alterada com sucesso!");
      message.setText("Sua senha no Omnibus foi alterada com sucesso.");
      mailSender.send(message);
    } catch (MailException e) {
      log.error("Failed to send password-reset notice to {}", email, e);
    }
  }
}
