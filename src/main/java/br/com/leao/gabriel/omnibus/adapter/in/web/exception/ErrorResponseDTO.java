package br.com.leao.gabriel.omnibus.adapter.in.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(Include.NON_EMPTY)
public record ErrorResponseDTO(
    OffsetDateTime timestamp,
    int status,
    String message,
    String traceId,
    List<FieldErrorDTO> fieldErrors) {

  public record FieldErrorDTO(String field, String message) {

  }
}