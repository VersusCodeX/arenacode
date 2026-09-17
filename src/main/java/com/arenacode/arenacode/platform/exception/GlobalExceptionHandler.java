package com.arenacode.arenacode.platform.exception;

import com.arenacode.arenacode.identity.application.EmailAlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage(),
                    (first, second) -> first));

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    problemDetail.setTitle("Validation failed");
    problemDetail.setType(URI.create("https://arenacode.dev/validation-error"));
    problemDetail.setProperty("errors", errors);

    return ResponseEntity.badRequest().body(problemDetail);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Constraint violation");
    problemDetail.setType(URI.create("https://arenacode.dev/constraint-violation"));

    return ResponseEntity.badRequest().body(problemDetail);
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problemDetail.setTitle("Email already exists");
    problemDetail.setType(URI.create("https://arenacode.dev/email-already-exists"));

    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
  }
}
