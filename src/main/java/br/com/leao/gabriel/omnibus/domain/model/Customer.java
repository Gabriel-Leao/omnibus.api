package br.com.leao.gabriel.omnibus.domain.model;

import java.time.Instant;
import java.time.LocalDate;
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
      Instant createdAt,
      Instant updatedAt,
      Instant deletedAt,
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
      Instant createdAt,
      Instant updatedAt,
      Instant deletedAt,
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

  public boolean canUseOtp(OtpType otpType) {
    return switch (otpType) {
      case ACCOUNT_ACTIVATION -> !isActivated();
      case PASSWORD_RESET, EMAIL_CHANGE -> isActivated();
    };
  }

  public Customer changePassword(String passwordHash) {
    Objects.requireNonNull(passwordHash, "Password hash must not be null");

    return new Customer(
        getId(),
        getName(),
        getEmail(),
        passwordHash,
        getStatus(),
        getCreatedAt(),
        getUpdatedAt(),
        getDeletedAt(),
        birthDate,
        photoUrl
    );
  }

  /**
   * Returns a copy of this customer transitioned to {@link UserStatus#ACTIVE}.
   */
  public Customer activate() {
    return new Customer(
        getId(),
        getName(),
        getEmail(),
        getPasswordHash(),
        UserStatus.ACTIVE,
        getCreatedAt(),
        Instant.now(),
        null,
        birthDate,
        photoUrl);
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
        Instant.now(),
        Instant.now(),
        birthDate,
        photoUrl);
  }

  public boolean isActivated() {
    return UserStatus.ACTIVE.equals(getStatus());
  }
}
