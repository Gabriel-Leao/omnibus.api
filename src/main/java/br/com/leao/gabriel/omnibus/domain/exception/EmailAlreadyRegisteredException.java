package br.com.leao.gabriel.omnibus.domain.exception;

public class EmailAlreadyRegisteredException extends ConflictException {

  public EmailAlreadyRegisteredException(String email) {
    super("Email already registered: " + email);
  }
}