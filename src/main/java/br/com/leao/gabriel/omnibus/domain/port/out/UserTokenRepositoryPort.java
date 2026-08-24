package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import java.util.Optional;

/**
 * Output port for persisting and retrieving {@link UserToken} instances.
 */
public interface UserTokenRepositoryPort {

  UserToken save(UserToken token);

  /**
   * Finds the most recent token for the given user and type, regardless of state.
   */
  Optional<UserToken> findLatestByUserIdAndType(String userId, OtpType type);

  void deleteById(Long id);

  void flush();

  Optional<UserToken> findActiveByUserId(String userId);
}
