package br.com.leao.gabriel.omnibus.adapter.in.web.security;

import br.com.leao.gabriel.omnibus.adapter.out.security.JwtTokenParser;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Inbound adapter that intercepts every request, extracts a JWT from the {@code Authorization}
 * header (if present), validates it, and populates the {@link SecurityContextHolder} so downstream
 * authorisation checks (e.g. {@code @PreAuthorize}) have access to the caller's identity and
 * authorities.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenParser tokenParser;

  public JwtAuthenticationFilter(JwtTokenParser tokenParser) {
    this.tokenParser = tokenParser;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");

    if (header != null && header.startsWith(BEARER_PREFIX)) {
      String token = header.substring(BEARER_PREFIX.length());
      try {
        authenticate(token);
      } catch (JwtException ex) {
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }

  private void authenticate(String token) {
    Claims claims = tokenParser.parseClaims(token);

    List<String> authorities =
        tokenParser.extractAuthorities(claims);

    if (authorities.contains("PASSWORD_RESET")) {
      OtpType purpose = tokenParser.extractPurpose(claims);

      if (purpose != OtpType.PASSWORD_RESET) {
        throw new JwtException("Invalid token purpose");
      }
    }

    var grantedAuthorities =
        authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .toList();

    var authentication =
        new UsernamePasswordAuthenticationToken(
            claims.getSubject(),
            null,
            grantedAuthorities);

    SecurityContextHolder.getContext()
        .setAuthentication(authentication);
  }
}
