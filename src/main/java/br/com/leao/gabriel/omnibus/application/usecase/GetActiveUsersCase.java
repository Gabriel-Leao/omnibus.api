package br.com.leao.gabriel.omnibus.application.usecase;

import br.com.leao.gabriel.omnibus.domain.model.User;
import java.util.List;

public interface GetActiveUsersCase {
  List<User> execute();
}
