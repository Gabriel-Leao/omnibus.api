package br.com.leao.gabriel.omnibus.adapter.out.security;

import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.StaffRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Loads user data for Spring Security's authentication mechanism. From the domain's perspective
 * this is an outbound adapter: it is invoked by the security framework to fetch external data
 * needed to authenticate, not by application code.
 */
@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final CustomerRepositoryPort customerRepository;
  private final StaffRepositoryPort staffRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return customerRepository
        .findByEmail(email)
        .<UserDetails>map(
            c -> UserPrincipal.ofCustomer(c.getId(), c.getEmail(), c.getPasswordHash(),
                c.getStatus()))
        .or(
            () ->
                staffRepository
                    .findByEmail(email)
                    .map(
                        s ->
                            UserPrincipal.ofStaff(
                                s.getId(),
                                s.getEmail(),
                                s.getPasswordHash(),
                                s.getStatus(),
                                s.getRole().name())))
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
  }
}