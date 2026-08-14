package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Base exception for forbidden domain operations.
 */
public abstract class ForbiddenException extends DomainException {

  protected ForbiddenException(String message) {
    super(message);
  }
}
