package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "User profile update request (all fields optional)")
public record UserUpdateDTO(
                @Schema(description = "Full name", example = "John Doe") String name,
                @Schema(description = "Short bio") String bio,
                @Schema(description = "LinkedIn profile URL") String linkedInUrl,
                @Schema(description = "Portfolio URL") String portfolioUrl,
                @Schema(description = "Skills list") List<String> skills,
                @Schema(description = "Years of experience") Integer experienceYears,
                @Schema(description = "Professional headline") String headline,
                @Schema(description = "Current location") String location,
                @Schema(description = "Company name") String companyName,
                @Schema(description = "Job designation") String designation,
                @Schema(description = "Company website URL") String companyWebsite) {
}
