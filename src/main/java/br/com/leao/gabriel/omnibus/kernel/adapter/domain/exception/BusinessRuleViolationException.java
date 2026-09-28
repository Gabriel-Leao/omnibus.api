package br.com.leao.gabriel.omnibus.kernel.adapter.domain.exception;

/**
 * Base exception for domain business rule violations.
 */
public abstract class BusinessRuleViolationException extends DomainException {

  /**
   * Handles the BusinessRuleViolationException operation.
   */
  protected BusinessRuleViolationException(String message) {
    super(message);
  }
}
