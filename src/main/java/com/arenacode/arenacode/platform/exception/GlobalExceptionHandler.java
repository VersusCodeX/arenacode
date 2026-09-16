package com.arenacode.arenacode.platform.exception;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tratamento global de excecoes para toda a API, seguindo RFC 9457 (Problem Details). Todas as
 * respostas de erro usam o tipo application/problem+json.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final URI PROBLEM_TYPE_BASE = URI.create("https://arenacode.dev/problems");

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
    List<ApiFieldViolation> violations =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new ApiFieldViolation(e.getField(), e.getDefaultMessage()))
            .toList();

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Falha de validacao de entrada");
    problem.setTitle("Validation Error");
    problem.setType(PROBLEM_TYPE_BASE.resolve("validation-error"));
    problem.setProperty("timestamp", Instant.now());
    problem.setProperty("violations", violations);

    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
    List<ApiFieldViolation> violations =
        ex.getConstraintViolations().stream()
            .map(v -> new ApiFieldViolation(v.getPropertyPath().toString(), v.getMessage()))
            .toList();

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Falha de validacao de entrada");
    problem.setTitle("Validation Error");
    problem.setType(PROBLEM_TYPE_BASE.resolve("validation-error"));
    problem.setProperty("timestamp", Instant.now());
    problem.setProperty("violations", violations);

    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleResourceNotFound(ResourceNotFoundException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setTitle("Resource Not Found");
    problem.setType(PROBLEM_TYPE_BASE.resolve("resource-not-found"));
    problem.setProperty("timestamp", Instant.now());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problem.setTitle("Conflict");
    problem.setType(PROBLEM_TYPE_BASE.resolve("conflict"));
    problem.setProperty("timestamp", Instant.now());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<ProblemDetail> handleBusinessRuleViolation(
      BusinessRuleViolationException ex) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    problem.setTitle("Business Rule Violation");
    problem.setType(PROBLEM_TYPE_BASE.resolve("business-rule-violation"));
    problem.setProperty("timestamp", Instant.now());

    return ResponseEntity.unprocessableEntity().body(problem);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ProblemDetail> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP nao suportado para este recurso");
    problem.setTitle("Method Not Allowed");
    problem.setType(PROBLEM_TYPE_BASE.resolve("method-not-allowed"));
    problem.setProperty("timestamp", Instant.now());

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocorreu um erro interno inesperado. Contate o suporte se o problema persistir.");
    problem.setTitle("Internal Server Error");
    problem.setType(PROBLEM_TYPE_BASE.resolve("internal-server-error"));
    problem.setProperty("timestamp", Instant.now());
    // Nao vaza stacktrace ou detalhes de infraestrutura para o cliente.

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }
}
