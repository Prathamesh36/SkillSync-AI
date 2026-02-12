package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI-parsed resume data extracted from uploaded document")
public record ParsedResumeDTO(
                @Schema(description = "Extracted full name") String fullName,
                @Schema(description = "Extracted email address") String email,
                @Schema(description = "Extracted skills list") List<String> skills,
                @Schema(description = "Estimated years of experience") Integer experienceYears,
                @Schema(description = "Education details") String education,
                @Schema(description = "Resume summary") String summary) {
}
