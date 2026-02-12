package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Candidate profile details")
public record CandidateProfileDTO(
        @Schema(description = "Technical skills") List<String> skills,
        @Schema(description = "Years of experience") Integer experienceYears,
        @Schema(description = "Professional headline") String headline,
        @Schema(description = "Current location") String location,
        @Schema(description = "Resume ID (null if not uploaded)") Long resumeId) {
}
