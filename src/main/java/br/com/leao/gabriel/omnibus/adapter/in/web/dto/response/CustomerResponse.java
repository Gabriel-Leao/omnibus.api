package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

import br.com.leao.gabriel.omnibus.domain.model.UserStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Represents the customer data returned by the web API.
 */
public record CustomerResponse(
    String id,
    String name,
    String email,
    UserStatus status,
    LocalDate birthDate,
    String photoUrl,
    OffsetDateTime createdAt) {

}
