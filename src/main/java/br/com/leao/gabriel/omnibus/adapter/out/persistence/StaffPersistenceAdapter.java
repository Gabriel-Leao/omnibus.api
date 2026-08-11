package br.com.leao.gabriel.omnibus.adapter.out.persistence;

import br.com.leao.gabriel.omnibus.adapter.out.mapper.StaffPersistenceMapper;
import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.StaffJpaEntity;
import br.com.leao.gabriel.omnibus.adapter.out.persistence.repository.StaffJpaRepository;
import br.com.leao.gabriel.omnibus.domain.model.Staff;
import br.com.leao.gabriel.omnibus.domain.port.out.StaffRepositoryPort;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StaffPersistenceAdapter implements StaffRepositoryPort {

  private final StaffJpaRepository jpaRepository;
  private final StaffPersistenceMapper mapper;

  @Override
  public Staff save(Staff staff) {
    StaffJpaEntity entity = mapper.toEntity(staff);
    StaffJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Staff> findById(UUID id) {
    if (id == null) {
      return Optional.empty();
    }
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Staff> findByEmail(String email) {
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

  @Override
  public boolean existsByEmployeeCode(String employeeCode) {
    if (employeeCode == null) {
      return false;
    }
    return jpaRepository.existsByEmployeeCode(employeeCode);
  }
}
