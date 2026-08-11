package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.Customer;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepositoryPort {

  Customer save(Customer customer);

  Optional<Customer> findById(UUID id);

  Optional<Customer> findByEmail(String email);

  boolean existsByEmail(String email);
}