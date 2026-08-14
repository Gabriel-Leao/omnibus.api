package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.Staff;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for staff persistence operations.
 */
public interface StaffRepositoryPort {

  /**
   * Saves a staff member.
   */
  Staff save(Staff staff);

  /**
   * Finds a staff member by identifier.
   */
  Optional<Staff> findById(UUID id);

  /**
   * Finds a staff member by email address.
   */
  Optional<Staff> findByEmail(String email);

  /**
   * Checks whether a staff member exists with the given email.
   */
  boolean existsByEmail(String email);

  /**
   * Checks whether a staff member exists with the given employee code.
   */
  boolean existsByEmployeeCode(String employeeCode);
}
