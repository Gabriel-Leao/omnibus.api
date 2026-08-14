package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for customer persistence operations.
 */
public interface CustomerRepositoryPort {

  /**
   * Saves a customer.
   */
  Customer save(Customer customer);

  /**
   * Finds a customer by identifier.
   */
  Optional<Customer> findById(UUID id);

  /**
   * Finds a customer by email address.
   */
  Optional<Customer> findByEmail(String email);

  /**
   * Checks whether a customer exists with the given email.
   */
  boolean existsByEmail(String email);
}
