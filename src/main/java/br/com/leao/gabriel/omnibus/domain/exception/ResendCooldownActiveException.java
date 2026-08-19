package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Thrown when a new verification code is requested before the resend cooldown has elapsed.
 */
public class ResendCooldownActiveException extends ConflictException {

  /**
   * Creates the exception with a message asking the caller to wait before retrying.
   */
  public ResendCooldownActiveException() {
    super("A code was recently sent; please wait before requesting another");
  }
}