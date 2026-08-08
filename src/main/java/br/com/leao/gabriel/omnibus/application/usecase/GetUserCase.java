package br.com.leao.gabriel.omnibus.application.usecase;

import br.com.leao.gabriel.omnibus.domain.model.User;
import java.util.UUID;

public interface GetUserCase {
  User execute(UUID userId);
}
