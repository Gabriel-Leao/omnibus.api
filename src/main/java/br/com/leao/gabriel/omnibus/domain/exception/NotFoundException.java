package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Base exception for resources or entities that cannot be found.
 */
public abstract class NotFoundException extends DomainException {
  /**
   * Handles the NotFoundException operation.
   */
  protected NotFoundException(String message) {
    super(message);
  }
}
