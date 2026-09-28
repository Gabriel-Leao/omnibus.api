package br.com.leao.gabriel.omnibus.notification.adapter.out;

import jakarta.mail.internet.MimeMessage;
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
 * Sends emails using SMTP via {@link JavaMailSender}.
 *
 * <p>This replaces the old {@code EmailSenderPort}/{@code SmtpEmailSenderAdapter} pair. That port
 * lived in {@code identity} and its methods took {@code Customer}/{@code OtpType} as parameters,
 * which meant this class had to import identity's domain model just to implement it — the exact
 * coupling a separate {@code notification} module is meant to avoid. There's no port here any more
 * because nothing outside this module calls this class directly: {@code identity} only ever
 * publishes events, and {@code IdentityNotificationListener} — the sole caller — lives in this same
 * module. A port exists to invert a dependency across a module boundary; within one module, a plain
 * class is enough.
 *
 * <p>{@link Async @Async} because the listener that calls this runs after the publishing module's
 * transaction has already committed and returned its HTTP response — mail delivery must never block
 * that. A failed send is only logged, in keeping with the original best-effort behaviour.
 */
@Component
@RequiredArgsConstructor
public class EmailNotifier {

  private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

  private final JavaMailSender mailSender;
  private final EmailTemplateRenderer templateRenderer;

  @Value("${spring.mail.from}")
  private String senderAddress;

  /**
   * Renders the named template with the given variables and sends the result as an HTML email.
   *
   * @param to           the recipient's email address
   * @param subject      the email subject line
   * @param templateName the template name, relative to {@code email/} — e.g.
   *                     {@code "identity/otp"}
   * @param variables    the variables made available to the template
   */
  @Async
  public void send(String to, String subject, String templateName, Map<String, Object> variables) {
    try {
      String bodyHtml = templateRenderer.render(templateName, variables);
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
}
