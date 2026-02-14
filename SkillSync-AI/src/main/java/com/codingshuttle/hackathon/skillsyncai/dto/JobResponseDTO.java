package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentType;
import com.codingshuttle.hackathon.skillsyncai.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Job posting details")
public record JobResponseDTO(
                @Schema(description = "Job ID") Long id,
                @Schema(description = "Job title") String title,
                @Schema(description = "Full description") String description,
                @Schema(description = "Company name") String companyName,
                @Schema(description = "Work location") String location,
                @Schema(description = "Minimum salary") BigDecimal salaryMin,
                @Schema(description = "Maximum salary") BigDecimal salaryMax,
                @Schema(description = "Salary currency") String currency,
                @Schema(description = "Work arrangement") JobType jobType,
                @Schema(description = "Employment type") EmploymentType employmentType,
                @Schema(description = "Recruiter user ID") Long recruiterId,
                @Schema(description = "Minimum required experience (years)") Integer minExperienceYears,
                @Schema(description = "Maximum required experience (years)") Integer maxExperienceYears,
                @Schema(description = "Required skills") List<String> skillsRequired,
                @Schema(description = "Application deadline") LocalDate applicationDeadline,
                @Schema(description = "Whether the job is currently active") boolean active,
                @Schema(description = "Posting timestamp") LocalDateTime createdAt,
                @Schema(description = "Unique reference ID") String jobReferenceId) {
}
