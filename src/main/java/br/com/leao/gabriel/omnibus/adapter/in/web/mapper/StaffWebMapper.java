package br.com.leao.gabriel.omnibus.adapter.in.web.mapper;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.StaffResponse;
import br.com.leao.gabriel.omnibus.domain.model.Staff;
import org.mapstruct.Mapper;

/**
 * Maps staff domain objects to web response DTOs.
 */
@Mapper(componentModel = "spring")
public interface StaffWebMapper {

  StaffResponse toResponse(Staff staff);
}
