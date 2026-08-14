package br.com.leao.gabriel.omnibus.adapter.out.persistence.entity;

import br.com.leao.gabriel.omnibus.domain.model.StaffDepartment;
import br.com.leao.gabriel.omnibus.domain.model.StaffRole;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing the staff profile.
 */
@Getter
@Setter
@Entity
@Table(name = "staff_profiles")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("STAFF")
public class StaffJpaEntity extends UserJpaEntity {

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StaffRole role;

  @Column(nullable = false, name = "employee_code")
  private String employeeCode;

  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private StaffDepartment department;

  @Column(nullable = false, name = "hired_at")
  private LocalDate hiredAt;
}
