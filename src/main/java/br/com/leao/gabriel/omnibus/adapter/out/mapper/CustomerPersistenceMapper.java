package br.com.leao.gabriel.omnibus.adapter.out.mapper;

import br.com.leao.gabriel.omnibus.adapter.out.persistence.entity.CustomerJpaEntity;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerPersistenceMapper {

  default CustomerJpaEntity toEntity(Customer customer) {
    CustomerJpaEntity entity = new CustomerJpaEntity();
    if (customer.getId() != null) {
      entity.setId(java.util.UUID.fromString(customer.getId()));
    }
    entity.setName(customer.getName());
    entity.setEmail(customer.getEmail());
    entity.setPasswordHash(customer.getPasswordHash());
    entity.setStatus(customer.getStatus());
    entity.setDeletedAt(customer.getDeletedAt());
    entity.setBirthDate(customer.getBirthDate());
    entity.setPhotoUrl(customer.getPhotoUrl());
    return entity;
  }

  default Customer toDomain(CustomerJpaEntity entity) {
    return Customer.reconstruct(
        entity.getId().toString(),
        entity.getName(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getDeletedAt(),
        entity.getBirthDate(),
        entity.getPhotoUrl());
  }
}