package br.com.leao.gabriel.omnibus.domain.port.out;

import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import java.util.Optional;

/**
 * Output port for persisting and retrieving {@link UserToken} instances.
 */
public interface UserTokenRepositoryPort {

  /**
   * Saves a user token.
   *
   * @param token the token to persist
   *
   * @return the persisted token
   */
  UserToken save(UserToken token);

  /**
   * Finds the most recent token for the given user and type, regardless of state.
   */
  Optional<UserToken> findLatestByUserIdAndType(String userId, OtpType type);

  /**
   * Deletes a user token by its identifier.
   *
   * @param id the token identifier
   */
  void deleteById(Long id);

  /**
   * Flushes pending token persistence operations.
   */
  void flush();

  /**
   * Finds the active token for a user.
   *
   * @param userId the user's identifier
   *
   * @return the active token, if one exists
   */
  Optional<UserToken> findActiveByUserId(String userId);
}
