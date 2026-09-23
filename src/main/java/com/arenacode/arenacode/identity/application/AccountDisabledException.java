package com.arenacode.arenacode.identity.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountDisabledException extends RuntimeException {
  public AccountDisabledException(String message) {
    super(message);
  }
}
