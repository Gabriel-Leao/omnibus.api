package br.com.leao.gabriel.omnibus.notification.adapter.out;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.dialect.SpringStandardDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Configures a standalone Thymeleaf {@link TemplateEngine} dedicated to rendering email bodies.
 *
 * <p>A dedicated engine, rather than the autoconfigured web view engine, keeps email rendering
 * independent of any Spring MVC view-resolution behaviour, since these templates are never served
 * as web pages.
 *
 * <p>The engine's default dialect is explicitly replaced with {@link SpringStandardDialect} so
 * that expressions (e.g. {@code ${name}}) are evaluated with SpringEL rather than OGNL. The plain
 * {@code StandardDialect} used by a default {@link TemplateEngine} requires the {@code ognl}
 * library, which is not on the classpath here; {@code SpringStandardDialect} relies on SpringEL
 * instead, which ships with {@code spring-core}.
 */
@Configuration
class ThymeleafEmailConfig {

  /**
   * Builds the {@link TemplateEngine} used to render templates under
   * {@code src/main/resources/notification/templates/email/}.
   *
   * <p>Templates that are specific to a given module's emails live in their own subfolder — e.g.
   * {@code email/identity/otp.html} — so that as other modules start sending notifications, their
   * templates don't collide with identity's. {@code email/layout.html} stays at the shared root
   * since it's generic chrome, not owned by any one module.
   *
   * @return the configured template engine
   */
  @Bean
  TemplateEngine emailTemplateEngine() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("notification/templates/");
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
