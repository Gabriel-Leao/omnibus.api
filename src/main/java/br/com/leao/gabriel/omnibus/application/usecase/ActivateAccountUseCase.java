package br.com.leao.gabriel.omnibus.application.usecase;

/**
 * Use case for activating a customer account.
 */
public interface ActivateAccountUseCase {

  /**
   * Activates a customer account and returns an access token.
   *
   * @param email the customer's email address
   *
   * @param code the submitted activation code
   *
   * @return a signed access token
   */
  String execute(String email, String code);
}
