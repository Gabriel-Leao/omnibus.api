package br.com.leao.gabriel.omnibus.adapter.in.web.mapper;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.CustomerResponse;
import br.com.leao.gabriel.omnibus.domain.model.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerWebMapper {

  CustomerResponse toResponse(Customer customer);
}