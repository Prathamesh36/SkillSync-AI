package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Full interview transcript with Q&A pairs")
public record InterviewTranscriptResponseDTO(
                @Schema(description = "Ordered list of interview messages (questions, answers, evaluations)") List<MessageDTO> messages,
                @Schema(description = "List of per-answer evaluations") List<EvaluationDTO> evaluations) {

        @Schema(description = "A single message in the interview transcript")
        public record MessageDTO(
                        @Schema(description = "Message role: AI or USER") String role,
                        @Schema(description = "Message content (question or answer text)") String content) {
        }
}
