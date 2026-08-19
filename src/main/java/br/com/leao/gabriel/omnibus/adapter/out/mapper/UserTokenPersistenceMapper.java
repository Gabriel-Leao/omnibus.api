package br.com.leao.gabriel.omnibus.adapter.out.mapper;

import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.UserTokenJpaEntity;
import br.com.leao.gabriel.omnibus.domain.model.UserToken;
import java.util.UUID;
import org.mapstruct.Mapper;

/**
 * Maps between {@link UserToken} and {@link UserTokenJpaEntity}.
 */
@Mapper(componentModel = "spring")
public interface UserTokenPersistenceMapper {

  /**
   * Converts a domain {@link UserToken} into its JPA entity representation.
   */
  default UserTokenJpaEntity toEntity(UserToken token) {
    return UserTokenJpaEntity.builder()
        .id(token.getId())
        .userId(UUID.fromString(token.getUserId()))
        .tokenHash(token.getCodeHash())
        .tokenType(token.getType())
        .targetEmail(token.getTargetEmail())
        .attempts(token.getAttempts())
        .expiresAt(token.getExpiresAt())
        .createdAt(token.getCreatedAt())
        .usedAt(token.getUsedAt())
        .build();
  }

  /**
   * Converts a {@link UserTokenJpaEntity} into its domain representation.
   */
  default UserToken toDomain(UserTokenJpaEntity entity) {
    return UserToken.reconstruct(
        entity.getId(),
        entity.getUserId().toString(),
        entity.getTokenHash(),
        entity.getTokenType(),
        entity.getTargetEmail(),
        entity.getAttempts(),
        entity.getExpiresAt(),
        entity.getCreatedAt(),
        entity.getUsedAt());
  }
}
