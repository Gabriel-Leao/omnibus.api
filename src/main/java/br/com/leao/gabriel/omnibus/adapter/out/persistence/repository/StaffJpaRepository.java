package br.com.leao.gabriel.omnibus.adapter.out.persistence.repository;

import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.StaffJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for staff persistence.
 */
public interface StaffJpaRepository extends JpaRepository<StaffJpaEntity, UUID> {

  /**
   * Finds a staff entity by email address.
   */
  Optional<StaffJpaEntity> findByEmail(String email);

  /**
   * Checks whether a staff entity exists with the given email.
   */
  boolean existsByEmail(String email);

  /**
   * Checks whether a staff entity exists with the given employee code.
   */
  boolean existsByEmployeeCode(String employeeCode);
}
