package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Indicates that an email address is already registered.
 */
public class EmailAlreadyRegisteredException extends ConflictException {

  /**
   * Creates an exception indicating that the given email is already registered.
   */
  public EmailAlreadyRegisteredException(String email) {
    super("Email already registered: " + email);
  }
}
