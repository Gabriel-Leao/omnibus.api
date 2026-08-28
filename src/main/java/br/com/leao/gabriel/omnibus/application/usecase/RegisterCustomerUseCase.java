package br.com.leao.gabriel.omnibus.application.usecase;

import java.time.LocalDate;

/**
 * Use case for registering a new customer account.
 *
 * <p>Deliberately returns no result: whether the email was already registered or not, the caller
 * receives no distinguishable outcome, preventing user enumeration via the registration endpoint's
 * response.
 */
public interface RegisterCustomerUseCase {

  /**
   * Registers a new customer account.
   *
   * @param name        the customer's name
   * @param email       the customer's email address
   * @param rawPassword the customer's plain-text password
   * @param birthDate   the customer's date of birth
   * @param photoUrl    the customer's optional profile photo URL
   */
  void execute(String name, String email, String rawPassword, LocalDate birthDate, String photoUrl);
}
