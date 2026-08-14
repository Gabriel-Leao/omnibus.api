package br.com.leao.gabriel.omnibus.domain.exception;

/**
 * Main initialisation class for the Omnibus API application.
 */
public abstract class NotFoundException extends DomainException {

  protected NotFoundException(String message) {
    super(message);
  }
}
