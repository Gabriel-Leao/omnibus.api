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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log =
      LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDTO> handleValidation(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {

    List<ErrorResponseDTO.FieldErrorDTO> fieldErrors =
        ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe ->
                new ErrorResponseDTO.FieldErrorDTO(
                    fe.getField(),
                    fe.getDefaultMessage()))
            .toList();

    log.warn(
        "Validation failed on {}: {}",
        request.getRequestURI(),
        fieldErrors);

    return build(
        HttpStatus.BAD_REQUEST,
        "Validation failed",
        fieldErrors);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDTO> handleMalformedJson(
      HttpServletRequest request) {

    log.warn(
        "Malformed request body on {}",
        request.getRequestURI());

    return build(
        HttpStatus.BAD_REQUEST,
        "Malformed request body",
        List.of());
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponseDTO> handleNotFound(
      NotFoundException ex,
      HttpServletRequest request) {

    log.warn(
        "Not found on {}: {}",
        request.getRequestURI(),
        ex.getMessage());

    return build(
        HttpStatus.NOT_FOUND,
        ex.getMessage(),
        List.of());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponseDTO> handleConflict(
      ConflictException ex,
      HttpServletRequest request) {

    log.warn(
        "Conflict on {}: {}",
        request.getRequestURI(),
        ex.getMessage());

    return build(
        HttpStatus.CONFLICT,
        ex.getMessage(),
        List.of());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponseDTO> handleForbidden(
      ForbiddenException ex,
      HttpServletRequest request) {

    log.warn(
        "Forbidden on {}: {}",
        request.getRequestURI(),
        ex.getMessage());

    return build(
        HttpStatus.FORBIDDEN,
        ex.getMessage(),
        List.of());
  }

  @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<ErrorResponseDTO> handleBusinessRule(
      BusinessRuleViolationException ex,
      HttpServletRequest request) {

    log.warn(
        "Business rule violated on {}: {}",
        request.getRequestURI(),
        ex.getMessage());

    return build(
        HttpStatus.BAD_REQUEST,
        ex.getMessage(),
        List.of());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
      IllegalArgumentException ex,
      HttpServletRequest request) {

    log.warn(
        "Illegal argument on {}: {}",
        request.getRequestURI(),
        ex.getMessage());

    return build(
        HttpStatus.BAD_REQUEST,
        ex.getMessage(),
        List.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> handleUnexpected(
      Exception ex,
      HttpServletRequest request) {

    log.error(
        "Unexpected error on {}",
        request.getRequestURI(),
        ex);

    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred",
        List.of());
  }

  private ResponseEntity<ErrorResponseDTO> build(
      HttpStatus status,
      String message,
      List<ErrorResponseDTO.FieldErrorDTO> fieldErrors) {

    String traceId =
        MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);

    ErrorResponseDTO body =
        new ErrorResponseDTO(
            OffsetDateTime.now(),
            status.value(),
            message,
            traceId,
            fieldErrors);

    return ResponseEntity
        .status(status)
        .body(body);
  }
}