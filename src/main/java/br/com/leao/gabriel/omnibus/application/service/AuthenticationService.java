package br.com.leao.gabriel.omnibus.application.service;

import br.com.leao.gabriel.omnibus.domain.exception.InvalidCredentialsException;
import br.com.leao.gabriel.omnibus.domain.model.AuthenticatedPrincipal;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.model.Staff;
import br.com.leao.gabriel.omnibus.domain.model.UserStatus;
import br.com.leao.gabriel.omnibus.domain.port.in.LoginUseCase;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.PasswordEncoderPort;
import br.com.leao.gabriel.omnibus.domain.port.out.StaffRepositoryPort;
import br.com.leao.gabriel.omnibus.domain.port.out.TokenIssuerPort;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Handles credential validation and access token issuance for both customer and staff accounts.
 *
 * <p>Account type is not known in advance by the caller: this service looks up the email across
 * both {@link CustomerRepositoryPort} and {@link StaffRepositoryPort} to determine which kind of
 * account is authenticating.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService implements LoginUseCase {

  private final CustomerRepositoryPort customerRepository;
  private final StaffRepositoryPort staffRepository;
  private final PasswordEncoderPort passwordEncoder;
  private final TokenIssuerPort tokenIssuer;

  @Override
  public String execute(String email, String rawPassword) {
    var customer = customerRepository.findByEmail(email);
    if (customer.isPresent()) {
      return authenticateCustomer(customer.get(), rawPassword);
    }

    var staff = staffRepository.findByEmail(email);
    if (staff.isPresent()) {
      return authenticateStaff(staff.get(), rawPassword);
    }

    throw new InvalidCredentialsException();
  }

  private String authenticateCustomer(Customer customer, String rawPassword) {
    validateCredentials(rawPassword, customer.getPasswordHash(), customer.getStatus());
    var principal =
        new AuthenticatedPrincipal(customer.getId(), customer.getEmail(), Set.of("ROLE_CUSTOMER"));
    return tokenIssuer.issueAccessToken(principal);
  }

  private String authenticateStaff(Staff staff, String rawPassword) {
    validateCredentials(rawPassword, staff.getPasswordHash(), staff.getStatus());
    var authority = "ROLE_" + staff.getRole().name();
    var principal =
        new AuthenticatedPrincipal(staff.getId(), staff.getEmail(), Set.of(authority));
    return tokenIssuer.issueAccessToken(principal);
  }

  private void validateCredentials(String rawPassword, String passwordHash, UserStatus status) {
    boolean passwordMatches = passwordEncoder.matches(rawPassword, passwordHash);
    boolean accountIsUsable = status == UserStatus.ACTIVE;
    if (!passwordMatches || !accountIsUsable) {
      throw new InvalidCredentialsException();
    }
  }
}