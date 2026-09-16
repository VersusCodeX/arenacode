package com.arenacode.arenacode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.OffsetDateTime;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Testes de integracao da migration baseline (V1__baseline.sql). Sobe um PostgreSQL real via
 * Testcontainers; o Flyway aplica as migrations automaticamente na inicializacao do contexto
 * Spring. Usa JDBC puro, sem entidades JPA, conforme escopo desta etapa.
 */
@SpringBootTest
@Testcontainers
class BaselineMigrationIntegrationTest {

  @Container @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  @Autowired private DataSource dataSource;

  @Test
  void flywaySchemaHistoryTableExists() throws Exception {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery(
                "SELECT to_regclass('public.flyway_schema_history') IS NOT NULL AS table_exists")) {
      assertTrue(resultSet.next());
      assertTrue(resultSet.getBoolean("table_exists"));
    }
  }

  @Test
  void appMetadataTableExists() throws Exception {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery(
                "SELECT to_regclass('public.app_metadata') IS NOT NULL AS table_exists")) {
      assertTrue(resultSet.next());
      assertTrue(resultSet.getBoolean("table_exists"));
    }
  }

  @Test
  void schemaVersionLabelSeedIsV1() throws Exception {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(
                "SELECT metadata_value FROM app_metadata WHERE metadata_key = ?")) {
      statement.setString(1, "schema_version_label");
      try (ResultSet resultSet = statement.executeQuery()) {
        assertTrue(resultSet.next());
        assertEquals("V1", resultSet.getString("metadata_value"));
      }
    }
  }

  @Test
  void triggerUpdatesUpdatedAtOnChange() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      OffsetDateTime before = readUpdatedAt(connection);

      Thread.sleep(50);

      try (PreparedStatement update =
          connection.prepareStatement(
              "UPDATE app_metadata SET metadata_value = ? WHERE metadata_key = ?")) {
        update.setString(1, "V1-check");
        update.setString(2, "schema_version_label");
        update.executeUpdate();
      }

      OffsetDateTime after = readUpdatedAt(connection);

      assertTrue(after.isAfter(before));
    }
  }

  private OffsetDateTime readUpdatedAt(Connection connection) throws Exception {
    try (PreparedStatement select =
        connection.prepareStatement("SELECT updated_at FROM app_metadata WHERE metadata_key = ?")) {
      select.setString(1, "schema_version_label");
      try (ResultSet resultSet = select.executeQuery()) {
        resultSet.next();
        return resultSet.getObject("updated_at", OffsetDateTime.class);
      }
    }
  }
}
