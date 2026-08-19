package br.com.leao.gabriel.omnibus.adapter.out.security;

import br.com.leao.gabriel.omnibus.domain.model.UserStatus;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security representation of an authenticated account (either a {@code Customer} or a
 * {@code Staff}). This class exists solely to satisfy the {@link UserDetails} contract expected by
 * Spring Security's authentication mechanism and is intentionally confined to the
 * {@code adapter.out.security} package — the domain model never depends on it.
 */
public final class UserPrincipal implements UserDetails {

  @Getter private final String id;
  private final String email;
  private final String passwordHash;
  private final UserStatus status;
  private final List<GrantedAuthority> authorities;

  private UserPrincipal(
      String id, String email, String passwordHash, UserStatus status, String roleAuthority) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
    this.status = status;
    this.authorities = List.of(new SimpleGrantedAuthority(roleAuthority));
  }

  /**
   * Builds a principal representing an authenticated {@code Customer}.
   */
  public static UserPrincipal ofCustomer(
      String id, String email, String passwordHash, UserStatus status) {
    return new UserPrincipal(id, email, passwordHash, status, "ROLE_CUSTOMER");
  }

  /**
   * Builds a principal representing an authenticated {@code Staff} member.
   */
  public static UserPrincipal ofStaff(
      String id, String email, String passwordHash, UserStatus status, String staffRole) {
    return new UserPrincipal(id, email, passwordHash, status, "ROLE_" + staffRole);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonLocked() {
    return status != UserStatus.SUSPENDED && status != UserStatus.BANNED;
  }

  @Override
  public boolean isEnabled() {
    return status == UserStatus.ACTIVE;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }
}
