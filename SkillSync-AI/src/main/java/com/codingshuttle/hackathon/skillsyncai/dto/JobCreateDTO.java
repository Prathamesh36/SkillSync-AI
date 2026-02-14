package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentType;
import com.codingshuttle.hackathon.skillsyncai.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Job posting creation/update request")
public record JobCreateDTO(
        @NotBlank @Schema(description = "Job title", example = "Senior Backend Developer") String title,
        @NotBlank @Schema(description = "Detailed job description") String description,
        @NotBlank @Schema(description = "Job location", example = "Mumbai, India") String location,
        @Schema(description = "Minimum salary", example = "1200000") BigDecimal salaryMin,
        @Schema(description = "Maximum salary", example = "2500000") BigDecimal salaryMax,
        @Schema(description = "Salary currency", example = "INR") String currency,
        @NotNull @Schema(description = "Work arrangement", example = "REMOTE") JobType jobType,
        @NotNull @Schema(description = "Employment type", example = "FULL_TIME") EmploymentType employmentType,
        @Schema(description = "Minimum required years of experience", example = "2") Integer minExperienceYears,
        @Schema(description = "Maximum required years of experience", example = "5") Integer maxExperienceYears,
        @Schema(description = "Required skills", example = "[\"Java\", \"Spring Boot\", \"PostgreSQL\"]") List<String> skillsRequired,
        @Schema(description = "Application deadline (ISO date)", example = "2026-03-31") java.time.LocalDate applicationDeadline,
        @NotNull(message = "Job Reference ID is required") @jakarta.validation.constraints.Pattern(regexp = "\\d+", message = "Job Reference ID must be numeric") @Schema(description = "Unique numeric reference ID", example = "10042") String jobReferenceId) {
}
