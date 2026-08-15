package br.com.leao.gabriel.omnibus.domain.port.in;

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
   * @throws br.com.leao.gabriel.omnibus.domain.exception.InvalidCredentialsException if the
   *                                                                                  email/password
   *                                                                                  combination is
   *                                                                                  invalid, or
   *                                                                                  the account is
   *                                                                                  not in a
   *                                                                                  loginable
   *                                                                                  state
   */
  String execute(String email, String rawPassword);
}