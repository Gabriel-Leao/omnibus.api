package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Thrown when login credentials are invalid or the account cannot currently authenticate.
 */
public class InvalidCredentialsException extends ForbiddenException {
  /**
   * Handles the InvalidCredentialsException operation.
   */
  public InvalidCredentialsException() {
    super("Invalid email or password");
  }
}
