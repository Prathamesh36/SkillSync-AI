package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Recruiter profile details")
public record RecruiterProfileDTO(
                @Schema(description = "Company name") String companyName,
                @Schema(description = "Job designation") String designation,
                @Schema(description = "Company website URL") String companyWebsite) {
}
