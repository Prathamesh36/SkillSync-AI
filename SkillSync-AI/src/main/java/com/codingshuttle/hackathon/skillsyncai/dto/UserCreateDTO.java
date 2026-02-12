package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "User registration request")
public record UserCreateDTO(
        @NotBlank @Email @Schema(description = "Email address (must be unique)", example = "john@example.com") String email,
        @NotBlank @Schema(description = "Account password", example = "securePass123") String password,
        @NotBlank @Schema(description = "Full name", example = "John Doe") String name,
        @NotNull @Schema(description = "Account role", example = "CANDIDATE") UserRole role,
        @Schema(description = "Short bio", example = "Full-stack developer with 5 years of experience") String bio,
        @Schema(description = "LinkedIn profile URL", example = "https://linkedin.com/in/johndoe") String linkedInUrl,
        @Schema(description = "Portfolio or personal website URL", example = "https://johndoe.dev") String portfolioUrl,
        @Schema(description = "Technical skills (for candidates)", example = "[\"Java\", \"Spring Boot\", \"React\"]") List<String> skills,
        @Schema(description = "Years of experience (for candidates)", example = "5") Integer experienceYears,
        @Schema(description = "Professional headline (for candidates)", example = "Senior Backend Developer") String headline,
        @Schema(description = "Current location", example = "Mumbai, India") String location,
        @Schema(description = "Company name (for recruiters)", example = "TechCorp Inc.") String companyName,
        @Schema(description = "Job designation (for recruiters)", example = "HR Manager") String designation,
        @Schema(description = "Company website (for recruiters)", example = "https://techcorp.com") String companyWebsite) {
}
