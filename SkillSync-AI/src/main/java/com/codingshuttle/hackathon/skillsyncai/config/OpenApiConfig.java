package com.codingshuttle.hackathon.skillsyncai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("SkillSync AI — API Reference")
                        .version("1.0.0")
                        .description(
                                "REST API for the SkillSync AI hiring platform. "
                                        + "Provides endpoints for authentication, user management, resume parsing, "
                                        + "job posting, AI-powered candidate matching, interview scheduling, "
                                        + "mock interview sessions, and job invitations.")
                        .contact(new Contact()
                                .name("Prathamesh Patil")
                                .url("https://github.com/Prathamesh36/SkillSync-AI")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token obtained from POST /api/auth/login")));
    }
}
