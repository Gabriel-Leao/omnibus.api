package br.com.leao.gabriel.omnibus.domain.exception;

import java.util.UUID;

/**
 * Thrown when a customer referenced by an already-validated context (such as a password-reset token
 * subject) can no longer be found. This is not a user-facing enumeration concern, as the caller has
 * already proven possession of a valid token issued for a real account.
 */
public class CustomerNotFoundException extends NotFoundException {

  /**
   * Creates the exception for the given customer identifier.
   *
   * @param customerId the identifier that could not be resolved to a customer
   */
  public CustomerNotFoundException(UUID customerId) {
    super("Customer not found: " + customerId);
  }
}
