package com.arenacode.arenacode.platform.exception;

/**
 * Excecao lancada quando um recurso solicitado nao e encontrado. Mapeada para HTTP 404 com resposta
 * Problem Details (RFC 9457).
 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String resource, String identifier) {
    super(resource + " com identificador '" + identifier + "' nao encontrado");
  }
}
