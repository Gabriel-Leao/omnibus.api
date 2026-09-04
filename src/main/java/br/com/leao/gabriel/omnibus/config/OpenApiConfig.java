package br.com.leao.gabriel.omnibus.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures the OpenAPI specification exposed by the application. */
@Configuration
public class OpenApiConfig {

  /**
   * Declares API metadata and the Bearer JWT scheme used by protected endpoints.
   *
   * @return the application's OpenAPI definition
   */
  @Bean
  public OpenAPI omnibusOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Omnibus API")
                .description("API for managing the Omnibus platform.")
                .version("v1"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
