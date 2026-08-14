package br.com.leao.gabriel.omnibus.adapter.out.mapper;

import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.StaffJpaEntity;
import br.com.leao.gabriel.omnibus.domain.model.Staff;
import java.util.UUID;
import org.mapstruct.Mapper;

/**
 * Maps staff domain objects to and from JPA entities.
 */
@Mapper(componentModel = "spring")
public interface StaffPersistenceMapper {

  /**
   * Maps a staff domain object to its JPA entity.
   */
  default StaffJpaEntity toEntity(Staff staff) {
    StaffJpaEntity entity = new StaffJpaEntity();
    if (staff.getId() != null) {
      entity.setId(UUID.fromString(staff.getId()));
    }
    entity.setName(staff.getName());
    entity.setEmail(staff.getEmail());
    entity.setPasswordHash(staff.getPasswordHash());
    entity.setStatus(staff.getStatus());
    entity.setDeletedAt(staff.getDeletedAt());
    entity.setRole(staff.getRole());
    entity.setEmployeeCode(staff.getEmployeeCode());
    entity.setDepartment(staff.getDepartment());
    entity.setHiredAt(staff.getHiredAt());
    return entity;
  }

  /**
   * Maps a staff JPA entity to its domain object.
   */
  default Staff toDomain(StaffJpaEntity entity) {
    return Staff.reconstruct(
        entity.getId().toString(),
        entity.getName(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getDeletedAt(),
        entity.getRole(),
        entity.getEmployeeCode(),
        entity.getDepartment(),
        entity.getHiredAt());
  }
}
