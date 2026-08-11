package br.com.leao.gabriel.omnibus.adapter.out.persistence.repository;

import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.CustomerJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {

  Optional<CustomerJpaEntity> findByEmail(String email);

  boolean existsByEmail(String email);
}
