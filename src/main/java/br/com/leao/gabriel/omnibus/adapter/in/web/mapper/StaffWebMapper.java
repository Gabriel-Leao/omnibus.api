package br.com.leao.gabriel.omnibus.adapter.in.web.mapper;

import br.com.leao.gabriel.omnibus.adapter.in.web.dto.response.StaffResponse;
import br.com.leao.gabriel.omnibus.domain.model.Staff;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StaffWebMapper {

  StaffResponse toResponse(Staff staff);
}