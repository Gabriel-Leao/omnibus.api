package br.com.leao.gabriel.omnibus.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import lombok.Getter;

/**
 * Domain model representing a customer account.
 */
@Getter
public class Customer extends UserAccount {

  private final LocalDate birthDate;
  private final String photoUrl;

  private Customer(
      String id,
      String name,
      String email,
      String passwordHash,
      UserStatus status,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt,
      OffsetDateTime deletedAt,
      LocalDate birthDate,
      String photoUrl) {
    super(
        id,
        name,
        email,
        passwordHash,
        status,
        AccountType.CUSTOMER,
        createdAt,
        updatedAt,
        deletedAt);
    this.birthDate = validateBirthDate(birthDate);
    this.photoUrl = photoUrl;
  }

  /**
   * Creates a new customer in the pending activation state.
   */
  public static Customer register(
      String name, String email, String passwordHash, LocalDate birthDate, String photoUrl) {
    return new Customer(
        null,
        name,
        email,
        passwordHash,
        UserStatus.PENDING_ACTIVATION,
        null,
        null,
        null,
        birthDate,
        photoUrl);
  }

  /**
   * Reconstructs a customer from persisted data.
   */
  public static Customer reconstruct(
      String id,
      String name,
      String email,
      String passwordHash,
      UserStatus status,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt,
      OffsetDateTime deletedAt,
      LocalDate birthDate,
      String photoUrl) {
    Objects.requireNonNull(id, "Id must not be null when reconstructing a persisted customer");
    Objects.requireNonNull(createdAt, "CreatedAt must not be null when reconstructing");
    Objects.requireNonNull(updatedAt, "UpdatedAt must not be null when reconstructing");
    return new Customer(
        id,
        name,
        email,
        passwordHash,
        status,
        createdAt,
        updatedAt,
        deletedAt,
        birthDate,
        photoUrl);
  }

  private static LocalDate validateBirthDate(LocalDate birthDate) {
    Objects.requireNonNull(birthDate, "Birth date must not be null");
    if (birthDate.isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Birth date cannot be in the future");
    }
    return birthDate;
  }

  /**
   * Creates a customer instance marked as pending deletion.
   */
  public Customer requestDeletion() {
    return new Customer(
        getId(),
        getName(),
        getEmail(),
        getPasswordHash(),
        UserStatus.PENDING_DELETION,
        getCreatedAt(),
        OffsetDateTime.now(),
        OffsetDateTime.now(),
        birthDate,
        photoUrl);
  }
}
