package br.com.leao.gabriel.omnibus.adapter.in.web.exception;

import br.com.leao.gabriel.omnibus.adapter.in.web.filter.TraceIdFilter;
import br.com.leao.gabriel.omnibus.domain.exception.BusinessRuleViolationException;
import br.com.leao.gabriel.omnibus.domain.exception.ConflictException;
import br.com.leao.gabriel.omnibus.domain.exception.ForbiddenException;
import br.com.leao.gabriel.omnibus.domain.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles exceptions raised by the web layer and converts them into HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Handles validation errors raised by request argument validation.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    List<ErrorResponseDto.FieldErrorDto> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ErrorResponseDto.FieldErrorDto(fe.getField(), fe.getDefaultMessage()))
            .toList();

    log.warn("Validation failed on {}: {}", request.getRequestURI(), fieldErrors);

    return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
  }

  /**
   * Handles requests with malformed JSON bodies.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDto> handleMalformedJson(HttpServletRequest request) {

    log.warn("Malformed request body on {}", request.getRequestURI());

    return build(HttpStatus.BAD_REQUEST, "Malformed request body", List.of());
  }

  /**
   * Handles not-found domain exceptions.
   */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleNotFound(
      NotFoundException ex, HttpServletRequest request) {

    log.warn("Not found on {}: {}", request.getRequestURI(), ex.getMessage());

    return build(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
  }

  /**
   * Handles domain conflict exceptions.
   */
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponseDto> handleConflict(
      ConflictException ex, HttpServletRequest request) {

    log.warn("Conflict on {}: {}", request.getRequestURI(), ex.getMessage());

    return build(HttpStatus.CONFLICT, ex.getMessage(), List.of());
  }

  /**
   * Handles forbidden domain exceptions.
   */
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponseDto> handleForbidden(
      ForbiddenException ex, HttpServletRequest request) {

    log.warn("Forbidden on {}: {}", request.getRequestURI(), ex.getMessage());

    return build(HttpStatus.FORBIDDEN, ex.getMessage(), List.of());
  }

  /**
   * Handles business rule violation exceptions.
   */
  @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<ErrorResponseDto> handleBusinessRule(
      BusinessRuleViolationException ex, HttpServletRequest request) {

    log.warn("Business rule violated on {}: {}", request.getRequestURI(), ex.getMessage());

    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
  }

  /**
   * Handles database constraint violations (e.g. unique-index races) as a safety net for cases that
   * slip past the application-level locking/validation.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, HttpServletRequest request) {

    log.warn("Data integrity violation on {}: {}", request.getRequestURI(), ex.getMessage());

    return build(HttpStatus.CONFLICT, "Solicitação já em andamento; tente novamente", List.of());
  }

  /**
   * Handles invalid argument exceptions.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponseDto> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {

    log.warn("Illegal argument on {}: {}", request.getRequestURI(), ex.getMessage());

    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
  }

  /**
   * Handles unexpected exceptions.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleUnexpected(
      Exception ex, HttpServletRequest request) {

    log.error("Unexpected error on {}", request.getRequestURI(), ex);

    return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", List.of());
  }

  private ResponseEntity<ErrorResponseDto> build(
      HttpStatus status, String message, List<ErrorResponseDto.FieldErrorDto> fieldErrors) {

    String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);

    ErrorResponseDto body =
        new ErrorResponseDto(OffsetDateTime.now(), status.value(), message, traceId, fieldErrors);

    return ResponseEntity.status(status).body(body);
  }
}