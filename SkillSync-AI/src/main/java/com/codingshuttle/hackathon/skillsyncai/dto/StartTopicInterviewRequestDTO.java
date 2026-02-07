package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.DifficultyLevel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for starting a topic-based mock interview.
 */
public record StartTopicInterviewRequestDTO(
        @NotEmpty(message = "Topics list cannot be empty") List<String> topics,

        @NotNull(message = "Difficulty level is required") DifficultyLevel difficulty) {
}
