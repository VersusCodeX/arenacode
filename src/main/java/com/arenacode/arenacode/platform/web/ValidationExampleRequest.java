package com.arenacode.arenacode.platform.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de exemplo para demonstracao de validacao de entrada e respostas Problem Details.
 * Nao representa um caso de uso real de dominio; sera removido ou substituido quando
 * endpoints de dominio forem implementados.
 */
public record ValidationExampleRequest(
		@NotBlank(message = "name e obrigatorio")
		@Size(min = 3, max = 80, message = "name deve ter entre 3 e 80 caracteres")
		String name,

		@NotNull(message = "quantity e obrigatorio")
		@Min(value = 1, message = "quantity deve ser maior ou igual a 1")
		Integer quantity
) {
}
