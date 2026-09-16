package com.arenacode.arenacode.platform.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller tecnico para demonstracao de validacao de entrada e documentacao OpenAPI.
 * Nao representa um endpoint de dominio real; sera removido ou substituido quando
 * endpoints de dominio forem implementados.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Endpoints tecnicos", description = "Endpoints para demonstracao e contrato inicial da API.")
public class ValidationExampleController {

	@PostMapping("/validation-example")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Exemplo de validacao de entrada", description = "Valida um payload de exemplo. Retorna 204 quando valido.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Payload valido"),
			@ApiResponse(responseCode = "400", description = "Payload invalido (Problem Details, RFC 9457)")
	})
	public ResponseEntity<Void> validationExample(@Valid @RequestBody ValidationExampleRequest request) {
		return ResponseEntity.noContent().build();
	}

}
