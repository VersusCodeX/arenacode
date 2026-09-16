package com.arenacode.arenacode.platform.exception;

/**
 * Excecao lancada quando uma regra de negocio e violada.
 * Mapeada para HTTP 422 (Unprocessable Entity) com resposta Problem Details (RFC 9457).
 * Alternativa: poderia ser 409; optamos por 422 para diferenciar conflitos de estado (409)
 * de violacoes semanticas de regra (422).
 */
public class BusinessRuleViolationException extends RuntimeException {

	public BusinessRuleViolationException(String message) {
		super(message);
	}

}
