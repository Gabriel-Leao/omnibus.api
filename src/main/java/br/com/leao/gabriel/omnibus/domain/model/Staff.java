package br.com.leao.gabriel.omnibus.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.Getter;

/**
 * Domain model representing a staff account.
 */
@Getter
public class Staff extends UserAccount {

  private final StaffRole role;
  private final String employeeCode;
  private final StaffDepartment department;
  private final LocalDate hiredAt;

  private Staff(
      String id,
      String name,
      String email,
      String passwordHash,
      UserStatus status,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt,
      OffsetDateTime deletedAt,
      StaffRole role,
      String employeeCode,
      StaffDepartment department,
      LocalDate hiredAt) {
    super(
        id, name, email, passwordHash, status, AccountType.STAFF, createdAt, updatedAt, deletedAt);
    this.role = Objects.requireNonNull(role, "Role must not be null");
    this.employeeCode = Objects.requireNonNull(employeeCode, "Employee code must not be null");
    this.department = department;
    this.hiredAt = Objects.requireNonNull(hiredAt, "Hired at must not be null");
  }

  /**
   * Creates a staff member with administrator privileges.
   */
  public static Staff createByAdmin(
      String name,
      String email,
      String passwordHash,
      StaffRole role,
      String employeeCode,
      StaffDepartment department) {
    return new Staff(
        null,
        name,
        email,
        passwordHash,
        UserStatus.ACTIVE,
        null,
        null,
        null,
        role,
        employeeCode,
        department,
        LocalDate.now());
  }

  /**
   * Reconstructs a staff member from persisted data.
   */
  public static Staff reconstruct(
      String id,
      String name,
      String email,
      String passwordHash,
      UserStatus status,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt,
      OffsetDateTime deletedAt,
      StaffRole role,
      String employeeCode,
      StaffDepartment department,
      LocalDate hiredAt) {
    Objects.requireNonNull(id, "Id must not be null when reconstructing a persisted staff");
    Objects.requireNonNull(createdAt, "CreatedAt must not be null when reconstructing");
    Objects.requireNonNull(updatedAt, "UpdatedAt must not be null when reconstructing");
    return new Staff(
        id,
        name,
        email,
        passwordHash,
        status,
        createdAt,
        updatedAt,
        deletedAt,
        role,
        employeeCode,
        department,
        hiredAt);
  }
}
