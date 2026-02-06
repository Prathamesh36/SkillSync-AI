package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.*;
import com.codingshuttle.hackathon.skillsyncai.service.InterviewSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for mock interview operations.
 * All endpoints require CANDIDATE role.
 */
@RestController
@RequestMapping("/api/interviews/mock")
@RequiredArgsConstructor
@Slf4j
public class InterviewController {

    private final InterviewSessionService interviewService;

    /**
     * Start a new mock interview session.
     * POST /api/interviews/mock/start
     */
    @PostMapping("/start")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<StartInterviewResponseDTO> startInterview(Authentication authentication) {
        String email = authentication.getName();
        log.info("Starting mock interview for: {}", email);

        StartInterviewResponseDTO response = interviewService.startInterview(email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Submit an answer to the current question.
     * POST /api/interviews/mock/{sessionId}/answer
     */
    @PostMapping("/{sessionId}/answer")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<SubmitAnswerResponseDTO> submitAnswer(
            Authentication authentication,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SubmitAnswerRequestDTO request) {

        String email = authentication.getName();
        log.info("Answer submitted for session: {}", sessionId);

        SubmitAnswerResponseDTO response = interviewService.submitAnswer(sessionId, request.answer(), email);
        return ResponseEntity.ok(response);
    }

    /**
     * End the interview and get final feedback.
     * POST /api/interviews/mock/{sessionId}/end
     */
    @PostMapping("/{sessionId}/end")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<EndInterviewResponseDTO> endInterview(
            Authentication authentication,
            @PathVariable UUID sessionId) {

        String email = authentication.getName();
        log.info("Ending interview session: {}", sessionId);

        EndInterviewResponseDTO response = interviewService.endInterview(sessionId, email);
        return ResponseEntity.ok(response);
    }
}
