package com.codingshuttle.hackathon.skillsyncai.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // Set environment variables from .env file
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
                log.info("Loaded environment variable: {}", entry.getKey());
            });

            log.info("Successfully loaded .env file with {} variables", dotenv.entries().size());
        } catch (Exception e) {
            log.warn("Failed to load .env file: {}. Using system environment variables.", e.getMessage());
        }
    }
}
