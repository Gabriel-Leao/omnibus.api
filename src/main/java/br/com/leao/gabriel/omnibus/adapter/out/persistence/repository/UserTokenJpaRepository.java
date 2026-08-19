package br.com.leao.gabriel.omnibus.adapter.out.persistence.repository;

import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.UserTokenJpaEntity;
import br.com.leao.gabriel.omnibus.domain.model.TokenType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link UserTokenJpaEntity}.
 */
public interface UserTokenJpaRepository extends JpaRepository<UserTokenJpaEntity, Long> {

  Optional<UserTokenJpaEntity> findFirstByUserIdAndTokenTypeOrderByCreatedAtDesc(
      UUID userId, TokenType tokenType);
}