package br.com.leao.gabriel.omnibus.adapter.in.web.validation.passwordmatches;

/**
 * Defines the password fields used by the password matching validator.
 */
public interface PasswordConfirmable {

  /**
   * Returns the password value.
   */
  String password();

  /**
   * Returns the password confirmation value.
   */
  String confirmPassword();
}
