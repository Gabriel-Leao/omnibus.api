package br.com.leao.gabriel.omnibus.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class User {

  private final UUID id;
  private final String name;
  private final String email;
  private final String passwordHash;
  private final UserStatus status;
  private final UserRole role;
  private final LocalDate birthDate;
  private final OffsetDateTime createdAt;
  private final OffsetDateTime updatedAt;
  private final String photoUrl;

  private User(
      UUID id,
      String name,
      String email,
      String passwordHash,
      String photoUrl,
      UserRole role,
      UserStatus status,
      LocalDate birthDate,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {

    this.id = id;
    this.name = Objects.requireNonNull(name, "Name must not be null");
    this.email = Objects.requireNonNull(email, "Email must not be null");
    this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash must not be null");
    this.photoUrl = photoUrl;
    this.role = Objects.requireNonNull(role, "Role must not be null");
    this.birthDate = validateBirthDate(birthDate);
    this.status = Objects.requireNonNull(status, "User Status must not be null");
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static User registerAsUser(
      String name, String email, String passwordHash, String photoUrl, LocalDate birthDate) {
    return new User(null, name, email, passwordHash, photoUrl, UserRole.USER,
        UserStatus.PENDING_ACTIVATION, birthDate, null,
        null);
  }

  public static User createByAdmin(
      String name, String email, String passwordHash, String photoUrl, UserRole role,
      LocalDate birthDate) {
    return new User(null, name, email, passwordHash, photoUrl, role, UserStatus.PENDING_ACTIVATION,
        birthDate, null, null);
  }

  public static User reconstruct(
      UUID id,
      String name,
      String email,
      String passwordHash,
      String photoUrl,
      UserRole role,
      UserStatus status,
      LocalDate birthDate,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    Objects.requireNonNull(id, "Id must not be null when reconstructing a persisted user");
    Objects.requireNonNull(createdAt,
        "CreatedAt must not be null when reconstructing a persisted user");
    Objects.requireNonNull(updatedAt,
        "UpdatedAt must not be null when reconstructing a persisted user");
    return new User(id, name, email, passwordHash, photoUrl, role, status, birthDate, createdAt,
        updatedAt);
  }

  private static LocalDate validateBirthDate(LocalDate birthDate) {
    Objects.requireNonNull(birthDate, "Birth date must not be null");

    if (birthDate.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Birth date cannot be in the future");
    }
    return birthDate;
  }
}
