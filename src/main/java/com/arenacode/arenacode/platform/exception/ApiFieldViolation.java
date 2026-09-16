package com.arenacode.arenacode.platform.exception;

/**
 * DTO para representar uma violacao de campo de Bean Validation em respostas Problem Details.
 * Alinhado ao formato esperado por consumidores de API e documentacao OpenAPI.
 */
public record ApiFieldViolation(String field, String message) {}
