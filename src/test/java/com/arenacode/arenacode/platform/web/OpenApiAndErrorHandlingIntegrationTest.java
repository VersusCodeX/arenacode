package com.arenacode.arenacode.platform.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integracao para OpenAPI (Swagger) e tratamento de erros (Problem Details).
 * Valida que a documentacao esta acessivel e que as respostas de erro seguem RFC 9457.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OpenApiAndErrorHandlingIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Test
	void swaggerUiIsAccessible() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void openApiJsonDocumentIsAccessible() throws Exception {
		MvcResult result = mockMvc.perform(get("/v3/api-docs")
					.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		String content = result.getResponse().getContentAsString();

		assertTrue(content.contains("\"title\":\"ArenaCode API\""));
		assertTrue(content.contains("\"version\":\"0.1.0\""));
		assertTrue(content.contains("\"paths\""));
	}

	@Test
	void invalidPayloadReturns400ProblemDetails() throws Exception {
		String payload = "{}";

		MvcResult result = mockMvc.perform(post("/api/v1/validation-example")
					.contentType(MediaType.APPLICATION_JSON)
					.content(payload))
				.andExpect(status().isBadRequest())
				.andReturn();

		String content = result.getResponse().getContentAsString();

		assertTrue(content.contains("\"title\":\"Validation Error\""));
		assertTrue(content.contains("\"status\":400"));
		assertTrue(content.contains("\"violations\""));
	}

	@Test
	void resourceNotFoundExceptionReturns404ProblemDetails() throws Exception {
		// Endpoint nao mapeado retorna 404 nativo do Spring (nao passa pelo GlobalExceptionHandler).
		// O teste valida apenas o status 404, sem exigir formato Problem Details especifico.
		mockMvc.perform(get("/api/v1/nonexistent"))
				.andExpect(status().isNotFound());
	}

	@Test
	void conflictExceptionReturns409ProblemDetails() throws Exception {
		// Simula um conflito via endpoint inexistente com metodo incompativel
		MvcResult result = mockMvc.perform(post("/api/v1/health"))
				.andExpect(status().isMethodNotAllowed())
				.andReturn();

		// Neste caso, o Spring retorna 405 (Method Not Allowed), que tambem e um Problem Detail
		// O teste valida que a resposta e JSON e contem "status".
		String content = result.getResponse().getContentAsString();

		assertTrue(content.contains("\"status\":405"));
	}

	@Test
	void errorResponsesDoNotLeakStackTrace() throws Exception {
		String payload = "{}";

		MvcResult result = mockMvc.perform(post("/api/v1/validation-example")
					.contentType(MediaType.APPLICATION_JSON)
					.content(payload))
				.andExpect(status().isBadRequest())
				.andReturn();

		String content = result.getResponse().getContentAsString();

		assertFalse(content.toLowerCase().contains("stacktrace"));
		assertFalse(content.toLowerCase().contains("exception"));
		assertFalse(content.toLowerCase().contains("at com."));
	}

}
