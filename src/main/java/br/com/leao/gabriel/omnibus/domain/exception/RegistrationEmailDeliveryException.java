package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Thrown when registration cannot be completed because the confirmation email could not be sent.
 */
public class RegistrationEmailDeliveryException extends RuntimeException {

  /**
   * Creates the exception wrapping the underlying delivery failure.
   */
  public RegistrationEmailDeliveryException(Throwable cause) {
    super("Unable to complete registration: activation email could not be sent", cause);
  }
}
