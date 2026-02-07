package com.codingshuttle.hackathon.skillsyncai.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for interview transcript including messages and evaluations.
 */
public record InterviewTranscriptResponseDTO(
        List<Map<String, String>> messages,
        List<Map<String, Object>> evaluations) {
}
