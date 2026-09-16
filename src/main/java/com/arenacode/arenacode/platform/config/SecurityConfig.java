package com.arenacode.arenacode.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracao minima de seguranca para endpoints de infraestrutura.
 * Libera acesso anonimo ao endpoint tecnico de saude (/api/v1/health),
 * aos endpoints do Actuator (health, info) e a documentacao OpenAPI/Swagger,
 * mantendo autenticacao para qualquer outra rota futura.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(
						"/api/v1/health",
						"/actuator/**",
						"/v3/api-docs",
						"/v3/api-docs/**",
						"/v3/api-docs.yaml",
						"/swagger-ui.html",
						"/swagger-ui/**"
				).permitAll()
				.anyRequest().authenticated())
			.csrf(csrf -> csrf.disable())
			.httpBasic(Customizer.withDefaults());

		return http.build();
	}

}
