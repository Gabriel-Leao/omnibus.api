package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.Staff;
import br.com.leao.gabriel.omnibus.domain.model.StaffRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrincipalFactoryTest {

  private static final String ID = "user-id";
  private static final String EMAIL = "gabriel@teste.com";

  private final PrincipalFactory factory = new PrincipalFactory();

  @Mock private Customer customer;

  @Mock private Staff staff;

  @Test
  @DisplayName("Should create customer principal with ROLE_CUSTOMER")
  void shouldCreateCustomerPrincipal() {
    when(customer.getId()).thenReturn(ID);
    when(customer.getEmail()).thenReturn(EMAIL);

    AuthenticatedPrincipal principal = factory.forCustomer(customer);

    assertThat(principal.id()).isEqualTo(ID);
    assertThat(principal.email()).isEqualTo(EMAIL);
    assertThat(principal.authorities()).containsExactly("ROLE_CUSTOMER");
  }

  @Test
  @DisplayName("Should create staff principal using the staff role")
  void shouldCreateStaffPrincipal() {
    when(staff.getId()).thenReturn(ID);
    when(staff.getEmail()).thenReturn(EMAIL);
    when(staff.getRole()).thenReturn(StaffRole.EDITOR);

    AuthenticatedPrincipal principal = factory.forStaff(staff);

    assertThat(principal.id()).isEqualTo(ID);
    assertThat(principal.email()).isEqualTo(EMAIL);
    assertThat(principal.authorities()).containsExactly("ROLE_EDITOR");
  }

  @Test
  @DisplayName("Should map every staff role to the corresponding authority")
  void shouldMapEveryStaffRoleToAuthority() {
    when(staff.getId()).thenReturn(ID);
    when(staff.getEmail()).thenReturn(EMAIL);

    for (StaffRole role : StaffRole.values()) {
      when(staff.getRole()).thenReturn(role);

      AuthenticatedPrincipal principal = factory.forStaff(staff);

      assertThat(principal.id()).isEqualTo(ID);
      assertThat(principal.email()).isEqualTo(EMAIL);
      assertThat(principal.authorities()).containsExactly("ROLE_" + role.name());
    }
  }
}
