package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Base exception for domain business rule violations.
 */
public abstract class BusinessRuleViolationException extends DomainException {

  protected BusinessRuleViolationException(String message) {
    super(message);
  }
}
