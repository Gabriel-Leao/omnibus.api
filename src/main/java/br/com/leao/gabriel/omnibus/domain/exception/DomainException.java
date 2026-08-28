package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Base exception for domain-level errors.
 */
public abstract class DomainException extends RuntimeException {
  /**
   * Handles the DomainException operation.
   */
  protected DomainException(String message) {
    super(message);
  }
}
