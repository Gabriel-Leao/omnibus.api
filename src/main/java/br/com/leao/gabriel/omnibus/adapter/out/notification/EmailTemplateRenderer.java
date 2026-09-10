package br.com.leao.gabriel.omnibus.adapter.out.notification;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Renders email bodies from the Thymeleaf templates stored under {@code templates/email/}.
 *
 * <p>This is a small infrastructure helper local to the SMTP adapter: it has no knowledge of
 * the domain and exists purely to keep HTML markup out of {@link SmtpEmailSenderAdapter}, in line
 * with the project's hexagonal architecture.
 */
@Component
@RequiredArgsConstructor
class EmailTemplateRenderer {

  private final TemplateEngine emailTemplateEngine;

  /**
   * Renders the named email template with the supplied variables.
   *
   * @param templateName the template name, relative to {@code templates/email/} and without the
   *                     {@code .html} suffix
   * @param variables    the variables made available to the template; a value may be {@code null}
   * @return the rendered HTML document
   */
  String render(String templateName, Map<String, Object> variables) {
    Context context = new Context();
    context.setVariables(variables);
    return emailTemplateEngine.process("email/" + templateName, context);
  }
}
