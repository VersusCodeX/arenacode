package com.arenacode.arenacode.identity.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

    @Bean
    @ConditionalOnMissingBean(name = "jwtSecretKey")
    public SecretKey jwtSecretKey(Environment env) {
        String secretValue = env.getProperty("JWT_SECRET", env.getProperty("app.jwt.secret", ""));
        if (secretValue == null || secretValue.isBlank()) {
            String activeProfile = env.getProperty("spring.profiles.active", "");
            if ("dev".equals(activeProfile)) {
                log.warn("JWT_SECRET not set. Using dev-only fallback. DO NOT use in production.");
                secretValue = "dev-secret-key-for-local-development-only-do-not-use-in-production";
            } else {
                throw new IllegalStateException("JWT_SECRET environment variable is required in non-dev profiles");
            }
        }
        if ("dev".equals(env.getProperty("spring.profiles.active", "")) && "dev-secret-key-for-local-development-only-do-not-use-in-production".equals(secretValue)) {
            log.warn("Using development JWT secret. This is insecure and must not be used in production.");
        }
        return new javax.crypto.spec.SecretKeySpec(secretValue.getBytes(StandardCharsets.UTF_8), "HMACSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        var jwkSource = new ImmutableSecret<SecurityContext>(jwtSecretKey);
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).build();
    }
}
