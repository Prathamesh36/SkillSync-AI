package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentType;
import com.codingshuttle.hackathon.skillsyncai.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "AI-recommended job for a candidate")
public record RecommendedJobResponse(
        @Schema(description = "Job ID") Long jobId,
        @Schema(description = "Job title") String title,
        @Schema(description = "Company name") String companyName,
        @Schema(description = "Job location") String location,
        @Schema(description = "Required skills") List<String> skillsRequired,
        @Schema(description = "Minimum salary") BigDecimal salaryMin,
        @Schema(description = "Maximum salary") BigDecimal salaryMax,
        @Schema(description = "Work arrangement") JobType jobType,
        @Schema(description = "Employment type") EmploymentType employmentType,
        @Schema(description = "Application status (APPLY_NOW, APPLIED, INVITED)") String applicationStatus,
        @Schema(description = "AI match score (0.0 - 1.0)") Double matchScore,
        @Schema(description = "Short AI explanation of match") String explanation,
        @Schema(description = "Minimum required experience in years") Integer minExperienceYears,
        @Schema(description = "Maximum required experience in years") Integer maxExperienceYears,
        @Schema(description = "Job posting date") java.time.LocalDateTime createdAt,
        @Schema(description = "Currency") String currency) {
}
