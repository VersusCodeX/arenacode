package com.arenacode.arenacode.platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao do OpenAPI (Swagger) para a ArenaCode API. Documento disponivel em /v3/api-docs
 * (JSON) e /v3/api-docs.yaml (YAML). Swagger UI disponivel em /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("ArenaCode API")
                .version("0.1.0")
                .description(
                    """
                                                    API REST do backend do ArenaCode, uma plataforma competitiva de programacao.
                                                    Este documento descreve os endpoints tecnicos iniciais e o padrao de respostas de erro.
                                                    Endpoints de dominio (usuarios, problemas, partidas, submissoes) serao adicionados
                                                    em commits futuros.
                                                    """)
                .contact(new Contact().name("ArenaCode Team").email("contato@arenacode.dev"))
                .license(
                    new License().name("A definir").url("https://opensource.org/licenses/MIT")))
        .servers(
            List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Ambiente local de desenvolvimento")))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Token JWT no formato 'Bearer <token>'. Obtido via POST /api/v1/auth/login. Necessario para acessar GET /api/v1/me e demais rotas protegidas.")));
  }
}
