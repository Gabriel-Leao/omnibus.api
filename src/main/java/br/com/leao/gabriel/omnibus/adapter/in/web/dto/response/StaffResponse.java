package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

import br.com.leao.gabriel.omnibus.domain.model.StaffDepartment;
import br.com.leao.gabriel.omnibus.domain.model.StaffRole;
import br.com.leao.gabriel.omnibus.domain.model.UserStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record StaffResponse(
    String id,
    String name,
    String email,
    UserStatus status,
    StaffRole role,
    String employeeCode,
    StaffDepartment department,
    LocalDate hiredAt,
    OffsetDateTime createdAt) {

}