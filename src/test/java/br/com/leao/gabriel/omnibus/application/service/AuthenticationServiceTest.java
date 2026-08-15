package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import br.com.leao.gabriel.omnibus.domain.exception.InvalidCredentialsException;
import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.Staff;
import br.com.leao.gabriel.omnibus.domain.model.StaffDepartment;
import br.com.leao.gabriel.omnibus.domain.model.StaffRole;
import br.com.leao.gabriel.omnibus.domain.model.UserStatus;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.PasswordEncoderPort;
import br.com.leao.gabriel.omnibus.domain.port.out.StaffRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link AuthenticationService}, covering both customer and staff login paths.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  private static final String EMAIL = "gabriel@teste.com";
  private static final String RAW_PASSWORD = "senha1234";
  private static final String PASSWORD_HASH = "hashed-password";
  private static final String ISSUED_TOKEN = "signed.jwt.token";

  @Mock
  private CustomerRepositoryPort customerRepository;
  @Mock
  private StaffRepositoryPort staffRepository;
  @Mock
  private PasswordEncoderPort passwordEncoder;
  @Mock
  private TokenIssuerPort tokenIssuer;

  private AuthenticationService service;

  @BeforeEach
  void setUp() {
    service =
        new AuthenticationService(customerRepository, staffRepository, passwordEncoder,
            tokenIssuer);
  }

  @Test
  @DisplayName("Should issue a token with ROLE_CUSTOMER for an active customer with valid credentials")
  void shouldAuthenticateActiveCustomer() {
    Customer customer = activeCustomer();
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
    when(tokenIssuer.issueAccessToken(any(AuthenticatedPrincipal.class))).thenReturn(ISSUED_TOKEN);

    String token = service.execute(EMAIL, RAW_PASSWORD);

    assertThat(token).isEqualTo(ISSUED_TOKEN);
  }

  @Test
  @DisplayName("Should issue a token with the staff's specific role for an active staff member")
  void shouldAuthenticateActiveStaffWithOwnRole() {
    Staff staff = activeStaff(StaffRole.EDITOR);
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(staffRepository.findByEmail(EMAIL)).thenReturn(Optional.of(staff));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
    when(tokenIssuer.issueAccessToken(any(AuthenticatedPrincipal.class))).thenReturn(ISSUED_TOKEN);

    String token = service.execute(EMAIL, RAW_PASSWORD);

    assertThat(token).isEqualTo(ISSUED_TOKEN);
  }

  @Test
  @DisplayName("Should reject login when the password does not match")
  void shouldRejectWrongPassword() {
    Customer customer = activeCustomer();
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(false);

    assertThrows(InvalidCredentialsException.class, () -> service.execute(EMAIL, RAW_PASSWORD));
  }

  @Test
  @DisplayName("Should reject login when the customer account is not yet active")
  void shouldRejectPendingActivationCustomer() {
    Customer customer = customerWithStatus(UserStatus.PENDING_ACTIVATION);
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);

    assertThrows(InvalidCredentialsException.class, () -> service.execute(EMAIL, RAW_PASSWORD));
  }

  @Test
  @DisplayName("Should reject login when no account exists for the given email")
  void shouldRejectUnknownEmail() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(staffRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThrows(InvalidCredentialsException.class, () -> service.execute(EMAIL, RAW_PASSWORD));
  }

  private Customer activeCustomer() {
    return customerWithStatus(UserStatus.ACTIVE);
  }

  private Customer customerWithStatus(UserStatus status) {
    return Customer.reconstruct(
        "customer-id",
        "Gabriel Leão",
        EMAIL,
        PASSWORD_HASH,
        status,
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        null,
        LocalDate.of(2000, 1, 1),
        null);
  }

  private Staff activeStaff(StaffRole role) {
    return Staff.reconstruct(
        "staff-id",
        "Gabriel Leão",
        EMAIL,
        PASSWORD_HASH,
        UserStatus.ACTIVE,
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        null,
        role,
        "EMP-001",
        StaffDepartment.IT,
        LocalDate.now());
  }
}