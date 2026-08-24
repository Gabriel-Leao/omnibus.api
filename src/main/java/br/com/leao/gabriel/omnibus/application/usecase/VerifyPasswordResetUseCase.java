package br.com.leao.gabriel.omnibus.application.usecase;

public interface VerifyPasswordResetUseCase {

  String execute(String email, String code);
}
