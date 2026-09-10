package br.com.leao.gabriel.omnibus.adapter.out.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Configures a standalone Thymeleaf {@link TemplateEngine} dedicated to rendering email bodies.
 *
 * <p>A dedicated engine, rather than the auto-configured web view engine, keeps email rendering
 * independent of any Spring MVC view-resolution behaviour, since these templates are never served
 * as web pages.
 *
 * <p>The engine's default dialect is explicitly replaced with {@link SpringStandardDialect} so
 * that expressions (e.g. {@code ${customer.name}}) are evaluated with SpringEL rather than OGNL.
 * The plain {@code StandardDialect} used by a default {@link TemplateEngine} requires the
 * {@code ognl} library, which is not on the classpath here; {@code SpringStandardDialect} relies on
 * SpringEL instead, which ships with {@code spring-core}.
 */
@Configuration
class ThymeleafEmailConfig {

  /**
   * Builds the {@link TemplateEngine} used to render templates under
   * {@code src/main/resources/templates/email/}.
   *
   * @return the configured template engine
   */
  @Bean
  TemplateEngine emailTemplateEngine() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");
    resolver.setCacheable(true);

    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(resolver);
    templateEngine.setDialect(new SpringStandardDialect());
    return templateEngine;
  }
}
