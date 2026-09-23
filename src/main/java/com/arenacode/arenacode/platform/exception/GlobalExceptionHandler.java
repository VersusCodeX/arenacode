package com.arenacode.arenacode.platform.exception;

import com.arenacode.arenacode.identity.application.AccountDisabledException;
import com.arenacode.arenacode.identity.application.AuthenticationException;
import com.arenacode.arenacode.identity.application.EmailAlreadyExistsException;
import java.net.URI;
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
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
    problemDetail.setTitle("Validation failed");
    return ResponseEntity.badRequest().body(problemDetail);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    problemDetail.setTitle("Authentication failed");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
  }

  @ExceptionHandler(AccountDisabledException.class)
  public ResponseEntity<ProblemDetail> handleAccountDisabled(AccountDisabledException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    problemDetail.setTitle("Account disabled");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
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
