package br.com.leao.gabriel.omnibus.application.usecase;

import java.util.UUID;

/**
 * Use case for resetting a customer's password.
 */
public interface ResetPasswordUseCase {

  /**
   * Resets the password for the specified customer.
   *
   * @param userId the customer's identifier
   *
   * @param newPassword the new plain-text password
   */
  void execute(UUID userId, String newPassword);
}
