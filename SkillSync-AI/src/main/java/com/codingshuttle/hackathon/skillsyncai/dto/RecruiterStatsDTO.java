package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Recruiter dashboard statistics")
public record RecruiterStatsDTO(
                @Schema(description = "Number of active job postings") long activeJobs,
                @Schema(description = "Total applications received across all jobs") long totalApplications,
                @Schema(description = "Number of scheduled interviews") long scheduledInterviews) {
}
