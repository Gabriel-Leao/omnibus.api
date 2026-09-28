package br.com.leao.gabriel.omnibus.notification.adapter.out;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Renders email bodies from the Thymeleaf templates stored under {@code notification/templates/}.
 *
 * <p>This is a small infrastructure helper local to {@link EmailNotifier}: it has no knowledge of
 * any module's domain and exists purely to keep HTML markup out of that class.
 */
@Component
@RequiredArgsConstructor
class EmailTemplateRenderer {

  private final TemplateEngine emailTemplateEngine;

  /**
   * Renders the named email template with the supplied variables.
   *
   * @param templateName the template name, relative to {@code email/} and without the
   *                     {@code .html} suffix — e.g. {@code "identity/otp"}
   * @param variables    the variables made available to the template; a value may be {@code null}
   * @return the rendered HTML document
   */
  String render(String templateName, Map<String, Object> variables) {
    Context context = new Context();
    context.setVariables(variables);
    return emailTemplateEngine.process("email/" + templateName, context);
  }
}
