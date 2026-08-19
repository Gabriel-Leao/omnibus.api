package br.com.leao.gabriel.omnibus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

  @Mock
  private PrincipalFactory principalFactory;

  @Mock
  private AuthenticatedPrincipal principal;

  private AuthenticationService service;

  @BeforeEach
  void setUp() {
    service =
        new AuthenticationService(
            customerRepository,
            staffRepository,
            passwordEncoder,
            tokenIssuer,
            principalFactory);
  }

  @Test
  @DisplayName("Should authenticate active customer")
  void shouldAuthenticateActiveCustomer() {
    Customer customer = activeCustomer();

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
    when(principalFactory.forCustomer(customer)).thenReturn(principal);
    when(tokenIssuer.issueAccessToken(principal)).thenReturn(ISSUED_TOKEN);

    String result = service.execute(EMAIL, RAW_PASSWORD);

    assertThat(result).isEqualTo(ISSUED_TOKEN);

    verify(principalFactory).forCustomer(customer);
    verify(tokenIssuer).issueAccessToken(principal);
    verifyNoInteractions(staffRepository);
  }

  @Test
  @DisplayName("Should authenticate active staff")
  void shouldAuthenticateActiveStaff() {
    Staff staff = activeStaff(StaffRole.EDITOR);

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(staffRepository.findByEmail(EMAIL)).thenReturn(Optional.of(staff));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
    when(principalFactory.forStaff(staff)).thenReturn(principal);
    when(tokenIssuer.issueAccessToken(principal)).thenReturn(ISSUED_TOKEN);

    String result = service.execute(EMAIL, RAW_PASSWORD);

    assertThat(result).isEqualTo(ISSUED_TOKEN);

    verify(principalFactory).forStaff(staff);
    verify(tokenIssuer).issueAccessToken(principal);
  }

  @Test
  @DisplayName("Should reject invalid customer password")
  void shouldRejectWrongCustomerPassword() {
    Customer customer = activeCustomer();

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(false);

    assertThrows(
        InvalidCredentialsException.class,
        () -> service.execute(EMAIL, RAW_PASSWORD));

    verifyNoInteractions(tokenIssuer, principalFactory);
  }

  @Test
  @DisplayName("Should reject invalid staff password")
  void shouldRejectWrongStaffPassword() {
    Staff staff = activeStaff(StaffRole.EDITOR);

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(staffRepository.findByEmail(EMAIL)).thenReturn(Optional.of(staff));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(false);

    assertThrows(
        InvalidCredentialsException.class,
        () -> service.execute(EMAIL, RAW_PASSWORD));

    verifyNoInteractions(tokenIssuer, principalFactory);
  }

  @Test
  @DisplayName("Should reject inactive customer")
  void shouldRejectInactiveCustomer() {
    Customer customer = customerWithStatus(UserStatus.PENDING_ACTIVATION);

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);

    assertThrows(
        InvalidCredentialsException.class,
        () -> service.execute(EMAIL, RAW_PASSWORD));

    verifyNoInteractions(tokenIssuer, principalFactory);
  }

  @Test
  @DisplayName("Should reject Pending staff")
  void shouldRejectInactiveStaff() {
    Staff staff =
        Staff.reconstruct(
            "staff-id",
            "Gabriel Leão",
            EMAIL,
            PASSWORD_HASH,
            UserStatus.PENDING_ACTIVATION,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null,
            StaffRole.EDITOR,
            "EMP-001",
            StaffDepartment.IT,
            LocalDate.now());

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(staffRepository.findByEmail(EMAIL)).thenReturn(Optional.of(staff));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);

    assertThrows(
        InvalidCredentialsException.class,
        () -> service.execute(EMAIL, RAW_PASSWORD));

    verifyNoInteractions(tokenIssuer, principalFactory);
  }

  @Test
  @DisplayName("Should reject unknown email")
  void shouldRejectUnknownEmail() {
    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
    when(staffRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThrows(
        InvalidCredentialsException.class,
        () -> service.execute(EMAIL, RAW_PASSWORD));

    verifyNoInteractions(passwordEncoder, tokenIssuer, principalFactory);
  }

  @Test
  @DisplayName("Should authenticate customer before checking staff")
  void shouldPreferCustomerWhenBothAccountsUseSameEmail() {
    Customer customer = activeCustomer();

    when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
    when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
    when(principalFactory.forCustomer(customer)).thenReturn(principal);
    when(tokenIssuer.issueAccessToken(principal)).thenReturn(ISSUED_TOKEN);

    String result = service.execute(EMAIL, RAW_PASSWORD);

    assertThat(result).isEqualTo(ISSUED_TOKEN);

    verifyNoInteractions(staffRepository);
    verify(principalFactory).forCustomer(customer);
    verify(principalFactory, never()).forStaff(any());
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