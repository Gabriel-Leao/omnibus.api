package br.com.leao.gabriel.omnibus.adapter.out.persistence.repository;

import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.StaffJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffJpaRepository extends JpaRepository<StaffJpaEntity, UUID> {

  Optional<StaffJpaEntity> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByEmployeeCode(String employeeCode);
}
