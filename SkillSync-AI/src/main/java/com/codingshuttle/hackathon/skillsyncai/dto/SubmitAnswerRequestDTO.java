package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Submit an answer to an interview question")
public record SubmitAnswerRequestDTO(
                @NotBlank @Schema(description = "The candidate's answer to the current question") String answer) {
}
