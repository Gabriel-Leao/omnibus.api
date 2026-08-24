package br.com.leao.gabriel.omnibus.adapter.out.persistence.repository;

import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.UserTokenJpaEntity;
import br.com.leao.gabriel.omnibus.domain.model.OtpType;
import br.com.leao.gabriel.omnibus.domain.model.TokenStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for {@link UserTokenJpaEntity}.
 */
public interface UserTokenJpaRepository extends JpaRepository<UserTokenJpaEntity, Long> {

  Optional<UserTokenJpaEntity> findFirstByUserIdAndOtpTypeOrderByCreatedAtDesc(
      UUID userId, OtpType otpType);

  /**
   * Finds the currently active token for a user, taking a pessimistic write lock on the row.
   *
   * <p>The lock forces concurrent issuance requests for the same user (e.g. a double-submitted
   * resend) to serialize instead of racing to revoke/insert around the
   * {@code ux_user_token_one_active} partial unique index.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT t FROM UserTokenJpaEntity t WHERE t.userId = :userId AND t.tokenStatus = :status")
  Optional<UserTokenJpaEntity> findByUserIdAndTokenStatusForUpdate(
      @Param("userId") UUID userId, @Param("status") TokenStatus status);
}