package br.com.leao.gabriel.omnibus.application.usecase;

import java.util.UUID;

public interface ResetPasswordUseCase {

  void execute(UUID userId, String newPassword);

}
