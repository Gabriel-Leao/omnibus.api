package br.com.leao.gabriel.omnibus.identity.application.usecase;

import br.com.leao.gabriel.omnibus.identity.domain.exception.InvalidCredentialsException;

/**
 * Use case for authenticating a user with email and password credentials.
 */
public interface LoginUseCase {

  /**
   * Authenticates a user and issues an access token upon success.
   *
   * @param email       the user's email
   * @param rawPassword the user's plain-text password, as submitted
   * @return a signed access token
   * @throws InvalidCredentialsException if the email/password combination is invalid, or the
   *                                     account is not in a loginable state
   */
  String execute(String email, String rawPassword);
}
