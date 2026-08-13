package br.com.leao.gabriel.omnibus.domain.exception;

public abstract class ConflictException extends DomainException {

  protected ConflictException(String message) {
    super(message);
  }
}