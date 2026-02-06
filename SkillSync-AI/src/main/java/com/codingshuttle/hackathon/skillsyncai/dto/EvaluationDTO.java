package com.codingshuttle.hackathon.skillsyncai.dto;

import java.util.List;

/**
 * DTO for per-question evaluation from AI.
 */
public record EvaluationDTO(
        int score,
        List<String> strengths,
        List<String> weaknesses) {
}
