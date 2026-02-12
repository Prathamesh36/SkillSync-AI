package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request to start a topic-based mock interview")
public record StartTopicInterviewRequestDTO(
                @NotEmpty @Schema(description = "Topics to be covered in the interview", example = "[\"Spring Boot\", \"Microservices\", \"System Design\"]") List<String> topics,
                @NotNull @Schema(description = "Interview difficulty level", example = "MEDIUM") DifficultyLevel difficulty) {
}
