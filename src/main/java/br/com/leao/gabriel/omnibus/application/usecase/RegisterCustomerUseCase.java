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

  void execute(String name, String email, String rawPassword, LocalDate birthDate, String photoUrl);
}
