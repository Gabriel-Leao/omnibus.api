package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Thrown when the maximum number of verification attempts for a token has been exceeded.
 */
public class VerificationAttemptsExceededException extends ForbiddenException {

  /**
   * Creates the exception with a message instructing the user to request a new code.
   */
  public VerificationAttemptsExceededException() {
    super("Maximum verification attempts exceeded; request a new code");
  }
}