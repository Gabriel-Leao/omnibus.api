package br.com.leao.gabriel.omnibus.adapter.out.persistence;

import br.com.leao.gabriel.omnibus.adapter.out.mapper.UserTokenPersistenceMapper;
import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.UserTokenJpaEntity;
import br.com.leao.gabriel.omnibus.adapter.out.persistence.repository.UserTokenJpaRepository;
import br.com.leao.gabriel.omnibus.domain.model.TokenType;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import br.com.leao.gabriel.omnibus.domain.port.out.UserTokenRepositoryPort;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Persistence adapter implementing {@link UserTokenRepositoryPort} using JPA/PostgreSQL.
 */
@Component
@RequiredArgsConstructor
public class UserTokenPersistenceAdapter implements UserTokenRepositoryPort {

  private final UserTokenJpaRepository jpaRepository;
  private final UserTokenPersistenceMapper mapper;

  @Override
  public UserToken save(UserToken token) {
    UserTokenJpaEntity entity = mapper.toEntity(token);
    UserTokenJpaEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<UserToken> findLatestByUserIdAndType(String userId, TokenType type) {
    return jpaRepository
        .findFirstByUserIdAndTokenTypeOrderByCreatedAtDesc(UUID.fromString(userId), type)
        .map(mapper::toDomain);
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }
}
