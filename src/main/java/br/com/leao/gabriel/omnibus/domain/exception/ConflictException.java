package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Base exception for domain conflicts.
 */
public abstract class ConflictException extends DomainException {
  /**
   * Handles the ConflictException operation.
   */
  protected ConflictException(String message) {
    super(message);
  }
}
