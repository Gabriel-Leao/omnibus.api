package br.com.leao.gabriel.omnibus.adapter.out.persistence.entity;

import br.com.leao.gabriel.omnibus.domain.model.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
public abstract class UserJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, name = "password_hash")
  private String passwordHash;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private UserStatus status;

  @Column(nullable = false, name = "created_at")
  private OffsetDateTime createdAt;

  @Column(nullable = false, name = "updated_at")
  private OffsetDateTime updatedAt;

  @Column(nullable = true, name = "deleted_at")
  private OffsetDateTime deletedAt;

  @PrePersist
  private void prePersist() {
    createdAt = OffsetDateTime.now();
    updatedAt = OffsetDateTime.now();
  }

  @PreUpdate
  private void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}
