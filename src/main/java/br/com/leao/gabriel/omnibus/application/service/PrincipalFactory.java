package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.Staff;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Builds {@link AuthenticatedPrincipal} instances from domain accounts, centralizing how each
 * account type maps to its authorities so this logic is not duplicated across services that issue
 * tokens (login, account activation, etc.).
 */
@Component
public class PrincipalFactory {

  /**
   * Builds the principal representing an authenticated {@link Customer}.
   */
  public AuthenticatedPrincipal forCustomer(Customer customer) {
    return new AuthenticatedPrincipal(
        customer.getId(), customer.getEmail(), Set.of("ROLE_CUSTOMER"));
  }

  /**
   * Builds the principal representing an authenticated {@link Staff} member.
   */
  public AuthenticatedPrincipal forStaff(Staff staff) {
    String authority = "ROLE_" + staff.getRole().name();
    return new AuthenticatedPrincipal(staff.getId(), staff.getEmail(), Set.of(authority));
  }
}
