package com.arenacode.arenacode.platform.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de integracao dos health checks: endpoint tecnico (/api/v1/health)
 * e Actuator (/actuator/health, /liveness, /readiness), com PostgreSQL real
 * via Testcontainers para validar o estado UP com a dependencia disponivel.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class HealthEndpointsIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void technicalHealthEndpointReturnsOk() {
		ResponseEntity<HealthCheckResponse> response = restTemplate.getForEntity("/api/v1/health", HealthCheckResponse.class);

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals("arenacode", response.getBody().application());
		assertEquals("UP", response.getBody().status());
		assertNotNull(response.getBody().timestamp());
	}

	@Test
	void technicalHealthEndpointDoesNotLeakInfrastructureDetails() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/health", String.class);

		String body = response.getBody().toLowerCase();
		assertFalse(body.contains("postgres"));
		assertFalse(body.contains("jdbc"));
		assertFalse(body.contains("datasource"));
		assertFalse(body.contains("hikari"));
		assertFalse(body.contains("flyway"));
	}

	@Test
	void actuatorHealthReturnsUpWhenDatabaseIsAvailable() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

		assertEquals(200, response.getStatusCode().value());
		assertTrue(response.getBody().contains("\"status\":\"UP\""));
	}

	@Test
	void actuatorLivenessReturnsUp() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health/liveness", String.class);

		assertEquals(200, response.getStatusCode().value());
		assertTrue(response.getBody().contains("\"status\":\"UP\""));
	}

	@Test
	void actuatorReadinessReflectsDatabaseAvailability() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health/readiness", String.class);

		assertEquals(200, response.getStatusCode().value());
		assertTrue(response.getBody().contains("\"status\":\"UP\""));
	}

}
