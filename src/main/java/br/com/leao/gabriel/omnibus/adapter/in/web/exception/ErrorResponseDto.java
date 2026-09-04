package br.com.leao.gabriel.omnibus.adapter.in.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Standardised API error response")
public record ErrorResponseDto(
    @Schema(description = "Error date and time", example = "2026-09-03T15:00:00-03:00")
        OffsetDateTime timestamp,
    @Schema(description = "HTTP status code", example = "400") int status,
    @Schema(description = "Error description", example = "Validation failed") String message,
    @Schema(description = "Identificador para rastreamento", example = "a1b2c3d4") String traceId,
    @Schema(description = "Validation errors associated with fields")
        List<FieldErrorDto> fieldErrors) {

  /**
   * Represents a validation error associated with a specific field.
   *
   * @param field   the name of the field that caused the validation error
   *
   * @param message the validation error message
   */
  @Schema(description = "Validation error associated with a field")
  public record FieldErrorDto(
      @Schema(description = "Field name", example = "email") String field,
      @Schema(description = "Validation message", example = "Invalid e-mail") String message) {}
}
