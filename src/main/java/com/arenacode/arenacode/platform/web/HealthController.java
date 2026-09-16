package com.arenacode.arenacode.platform.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Controller tecnico de saude da aplicacao.
 * Fornece uma verificacao simples e independente do Actuator, pensada para
 * consumidores externos leves (load balancers simples, scripts de smoke test),
 * sem expor nenhum detalhe de infraestrutura (banco, pool de conexoes, etc.).
 * Para health checks completos com componentes de dependencia, use o
 * Spring Boot Actuator (/actuator/health, /actuator/health/liveness,
 * /actuator/health/readiness).
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

	private static final String APPLICATION_NAME = "arenacode";
	private static final String STATUS_UP = "UP";

	@GetMapping("/health")
	public HealthCheckResponse health() {
		return new HealthCheckResponse(APPLICATION_NAME, STATUS_UP, Instant.now());
	}

}
