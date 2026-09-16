package com.arenacode.arenacode.platform.web;

import java.time.Instant;

/**
 * DTO de resposta do endpoint tecnico de saude (GET /api/v1/health).
 * Endpoint simples e sem detalhes de infraestrutura; NAO substitui o
 * Spring Boot Actuator, que permanece como fonte completa de health checks
 * (/actuator/health, /actuator/health/liveness, /actuator/health/readiness).
 */
public record HealthCheckResponse(String application, String status, Instant timestamp) {
}
