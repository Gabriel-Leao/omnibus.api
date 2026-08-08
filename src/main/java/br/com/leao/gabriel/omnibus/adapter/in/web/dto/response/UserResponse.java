package br.com.leao.gabriel.omnibus.adapter.in.web.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    String photoUrl,
    LocalDate birthDate
) {

}