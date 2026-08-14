package br.com.leao.gabriel.omnibus.domain.port.in;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import java.time.LocalDate;

/**
 * Input port for registering a customer.
 */
public interface RegisterCustomerUseCase {

  /**
   * Registers a customer using the supplied registration data.
   */
  Customer execute(String name, String email, String rawPassword, LocalDate birthDate,
      String photoUrl);
}
