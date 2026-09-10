package br.com.leao.gabriel.omnibus.adapter.out.notification;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.port.out.EmailSenderPort;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
 *
 * <p>Email markup lives entirely in the Thymeleaf templates under
 * {@code src/main/resources/templates/email/} and is rendered by {@link EmailTemplateRenderer};
 * this class is concerned only with which template to use and which variables to pass it.
 */
@Component
@RequiredArgsConstructor
public class SmtpEmailSenderAdapter implements EmailSenderPort {

  private static final Logger log = LoggerFactory.getLogger(SmtpEmailSenderAdapter.class);

  private final JavaMailSender mailSender;
  private final EmailTemplateRenderer templateRenderer;

  @Value("${spring.mail.from}")
  private String senderAddress;

  /**
   * Sends a one-time password to the specified customer.
   *
   * @param customer the customer receiving the OTP
   * @param code     the plain-text OTP
   * @param otpType  the purpose of the OTP
   */
  @Override
  @Async
  public void sendOtp(Customer customer, String code, OtpType otpType) {
    String subject = otpType.getEmailSubject();
    Map<String, Object> variables = new HashMap<>();
    variables.put("subject", subject);
    variables.put("tag", otpType.getEmailTag());
    variables.put("name", customer.getName());
    variables.put("code", code);
    String body = templateRenderer.render("otp", variables);
    send(customer.getEmail(), subject, body);
  }

  /**
   * Sends a notice when an already registered email address is used for registration.
   *
   * @param email the email address that was already registered
   */
  @Override
  @Async
  public void sendDuplicateRegistrationNotice(String email) {
    String subject = "Tentativa de cadastro na Omnibus";
    String body = templateRenderer.render("duplicate-registration", Map.of("subject", subject));
    send(email, subject, body);
  }

  /**
   * Sends a notice confirming that a customer's password has been changed successfully.
   *
   * @param email the email address of the customer whose password was reset
   */
  @Override
  @Async
  public void sendPasswordResetNotice(String email) {
    String subject = "Sua senha foi alterada com sucesso!";
    String body = templateRenderer.render("password-reset", Map.of("subject", subject));
    send(email, subject, body);
  }

  /**
   * Builds a {@link MimeMessage} from a pre-rendered HTML body and dispatches it via
   * {@link JavaMailSender}.
   *
   * <p>Any failure is logged rather than propagated, in keeping with the asynchronous,
   * best-effort delivery behaviour documented on the class itself.
   *
   * @param to       the recipient's email address
   * @param subject  the email subject line
   * @param bodyHtml the rendered HTML body to send
   */
  private void send(String to, String subject, String bodyHtml) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
      helper.setFrom(senderAddress);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(bodyHtml, true);
      mailSender.send(mimeMessage);
    } catch (Exception e) {
      log.error("Failed to send email (subject='{}') to {}", subject, to, e);
    }
  }

  /**
   * Sends a purely informational notice confirming that a customer has completed registration.
   *
   * <p>No action is required from the customer; this is a courtesy notification only.
   *
   * @param customer the customer who has just completed registration
   */
  @Override
  @Async
  public void sendRegistrationConfirmation(Customer customer) {
    String subject = "Bem-vindo(a) à Omnibus!";
    Map<String, Object> variables = new HashMap<>();
    variables.put("subject", subject);
    variables.put("name", customer.getName());
    String body = templateRenderer.render("registration-confirmation", variables);
    send(customer.getEmail(), subject, body);
  }
}
