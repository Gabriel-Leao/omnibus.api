package br.com.leao.gabriel.omnibus.domain.exception;

public abstract class ForbiddenException extends DomainException {

  protected ForbiddenException(String message) {
    super(message);
  }
}