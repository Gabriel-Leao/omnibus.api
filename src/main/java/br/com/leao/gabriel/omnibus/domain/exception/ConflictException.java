package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Base exception for domain conflicts.
 */
public abstract class ConflictException extends DomainException {

  protected ConflictException(String message) {
    super(message);
  }
}
