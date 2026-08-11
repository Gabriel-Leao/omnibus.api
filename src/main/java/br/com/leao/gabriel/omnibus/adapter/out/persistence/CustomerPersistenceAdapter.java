package br.com.leao.gabriel.omnibus.adapter.out.persistence;

import br.com.leao.gabriel.omnibus.adapter.out.mapper.CustomerPersistenceMapper;
import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.CustomerJpaEntity;
import br.com.leao.gabriel.omnibus.adapter.out.persistence.repository.CustomerJpaRepository;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import br.com.leao.gabriel.omnibus.domain.port.out.CustomerRepositoryPort;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

  private final CustomerJpaRepository jpaRepository;
  private final CustomerPersistenceMapper mapper;

  @Override
  public Customer save(Customer customer) {
    CustomerJpaEntity entity = mapper.toEntity(customer);
    CustomerJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Customer> findById(UUID id) {
    if (id == null) {
      return Optional.empty();
    }
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Customer> findByEmail(String email) {
    if (email == null) {
      return Optional.empty();
    }
    return jpaRepository.findByEmail(email).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    if (email == null) {
      return false;
    }
    return jpaRepository.existsByEmail(email);
  }
}
