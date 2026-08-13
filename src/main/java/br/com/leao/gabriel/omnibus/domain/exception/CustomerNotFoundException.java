package br.com.leao.gabriel.omnibus.domain.exception;

public class CustomerNotFoundException extends NotFoundException {

  public CustomerNotFoundException(String id) {
    super("Customer not found: " + id);
  }
}