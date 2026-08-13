package br.com.leao.gabriel.omnibus.domain.exception;

public abstract class NotFoundException extends DomainException {

  protected NotFoundException(String message) {
    super(message);
  }
}