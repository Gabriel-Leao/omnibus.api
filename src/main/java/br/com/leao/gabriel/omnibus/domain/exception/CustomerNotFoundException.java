package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Indicates that a requested customer could not be found.
 */
public class CustomerNotFoundException extends NotFoundException {

  /**
   * Creates an exception indicating that the customer with the given identifier was not found.
   */
  public CustomerNotFoundException(String id) {
    super("Customer not found: " + id);
  }
}
