package com.arenacode.arenacode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ArenacodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArenacodeApplication.class, args);
    }
}
