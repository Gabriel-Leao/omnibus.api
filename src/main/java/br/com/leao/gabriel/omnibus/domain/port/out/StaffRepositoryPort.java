package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.Staff;
import java.util.Optional;

public interface StaffRepositoryPort {

  Staff save(Staff staff);

  Optional<Staff> findById(String id);

  Optional<Staff> findByEmail(String email);

  boolean existsByEmployeeCode(String employeeCode);
}