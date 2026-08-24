package br.com.leao.gabriel.omnibus.application.usecase;

public interface ActivateAccountUseCase {

  String execute(String email, String code);
}
