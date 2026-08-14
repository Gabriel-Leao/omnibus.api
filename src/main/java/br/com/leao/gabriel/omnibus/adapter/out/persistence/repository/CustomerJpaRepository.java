package br.com.leao.gabriel.omnibus.adapter.out.persistence.repository;

import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.CustomerJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for customer persistence.
 */
public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {

  /**
   * Finds a customer entity by email address.
   */
  Optional<CustomerJpaEntity> findByEmail(String email);

  /**
   * Checks whether a customer entity exists with the given email.
   */
  boolean existsByEmail(String email);
}
