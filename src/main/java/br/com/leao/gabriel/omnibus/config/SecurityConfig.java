package br.com.leao.gabriel.omnibus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configures the application's Spring Security settings.
 */
@Configuration
public class SecurityConfig {

  /**
   * Configures the HTTP security filter chain.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // TODO(Etapa 3): restringir por rota assim que JWT estiver implementado
            .anyRequest().permitAll()
        );
    return http.build();
  }
}
