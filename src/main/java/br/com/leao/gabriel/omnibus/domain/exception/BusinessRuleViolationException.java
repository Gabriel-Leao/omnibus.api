package br.com.leao.gabriel.omnibus.domain.exception;

public abstract class BusinessRuleViolationException extends DomainException {

  protected BusinessRuleViolationException(String message) {
    super(message);
  }
}