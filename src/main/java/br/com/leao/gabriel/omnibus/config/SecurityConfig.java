package br.com.leao.gabriel.omnibus.config;

import br.com.leao.gabriel.omnibus.adapter.in.web.security.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Central Spring Security wiring: filter chain, role hierarchy, and framework-level beans.
 */
@Configuration
@EnableMethodSecurity // enables @PreAuthorize on service/controller methods
public class SecurityConfig {

  @Value("${app.frontend-url}")
  private String frontendUrl;

  /**
   * Declares the staff role hierarchy: an ADMIN implicitly holds every authority granted to an
   * EDITOR, which in turn implicitly holds every authority granted to a MANAGER, and so on. A
   * single {@code hasRole("VIEWER")} check therefore also admits MANAGER, EDITOR and ADMIN.
   */
  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role("ADMIN")
        .implies("EDITOR")
        .role("EDITOR")
        .implies("MANAGER")
        .role("MANAGER")
        .implies("VIEWER")
        .build();
  }

  /**
   * Provides the BCrypt password encoder used across the application.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configures the application's HTTP security filter chain.
   *
   * <p>The application is stateless and uses JWT authentication. Authentication endpoints under
   * {@code /auth/**} are publicly accessible, while the JWT filter handles authentication for
   * protected endpoints.
   *
   * @param http                    the HTTP security configuration
   * @param jwtAuthenticationFilter the filter responsible for JWT authentication
   * @return the configured security filter chain
   * @throws Exception if the security configuration cannot be built
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/password-reset/confirm")
                    .hasAuthority("PASSWORD_RESET")
                    .requestMatchers(HttpMethod.POST, "/password-reset", "/password-reset/verify")
                    .permitAll()
                    .requestMatchers("/auth/**")
                    .permitAll()
                    .anyRequest()
                    .permitAll())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Configures the Cross-Origin Resource Sharing (CORS) policy for the application.
   *
   * <p>Only requests originating from the configured frontend URL are allowed. The configuration
   * permits the HTTP methods and headers required by the application and enables credentials so
   * that authenticated requests can include them.
   *
   * @return the CORS configuration source applied to all application endpoints
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(frontendUrl));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
