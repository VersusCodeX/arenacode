package com.arenacode.arenacode.platform.exception;

/**
 * Excecao lancada quando uma operacao resulta em conflito com o estado atual do recurso.
 * Mapeada para HTTP 409 com resposta Problem Details (RFC 9457).
 */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}

}
