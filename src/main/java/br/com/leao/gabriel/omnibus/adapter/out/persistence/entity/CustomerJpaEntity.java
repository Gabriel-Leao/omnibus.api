package br.com.leao.gabriel.omnibus.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing the customer profile.
 */
@Getter
@Setter
@Entity
@Table(name = "customer_profiles")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("CUSTOMER")
public class CustomerJpaEntity extends UserJpaEntity {

  @Column(nullable = false, name = "birth_date")
  private LocalDate birthDate;

  @Column(nullable = true, name = "photo_url")
  private String photoUrl;
}
