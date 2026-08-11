package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.Staff;
import java.util.Optional;
import java.util.UUID;

public interface StaffRepositoryPort {

  Staff save(Staff staff);

  Optional<Staff> findById(UUID id);

  Optional<Staff> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByEmployeeCode(String employeeCode);
}