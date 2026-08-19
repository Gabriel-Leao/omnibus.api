package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Thrown when a submitted verification code does not match, has expired, or has already been
 * consumed. Deliberately generic to avoid revealing which specific condition failed.
 */
public class InvalidVerificationCodeException extends ForbiddenException {

  /**
   * Creates the exception with a generic, non-revealing message.
   */
  public InvalidVerificationCodeException() {
    super("Invalid or expired verification code");
  }
}
