package br.com.leao.gabriel.omnibus.adapter.in.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents the error response returned by the web layer.
 *
 * @param timestamp   the date and time when the error occurred
 *
 * @param status      the HTTP status code
 *
 * @param message     the error message
 *
 * @param traceId     the unique identifier used to trace the request
 *
 * @param fieldErrors the validation errors associated with specific fields
 */
@JsonInclude(Include.NON_EMPTY)
public record ErrorResponseDto(
    OffsetDateTime timestamp,
    int status,
    String message,
    String traceId,
    List<FieldErrorDto> fieldErrors) {

  /**
   * Represents a validation error associated with a specific field.
   *
   * @param field   the name of the field that caused the validation error
   *
   * @param message the validation error message
   */
  public record FieldErrorDto(String field, String message) {}
}
