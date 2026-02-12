package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.*;
import com.codingshuttle.hackathon.skillsyncai.service.InterviewSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for mock interview operations.
 * All endpoints require CANDIDATE role.
 */
@RestController
@RequestMapping("/api/interviews/mock")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Mock Interviews", description = "AI-powered mock interview sessions — resume-based and topic-based")
public class InterviewController {

    private final InterviewSessionService interviewService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Start resume-based interview", description = "Starts a mock interview session where AI generates questions tailored to the candidate's resume and skills")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session started with first question"),
            @ApiResponse(responseCode = "400", description = "No resume uploaded")
    })
    public ResponseEntity<StartInterviewResponseDTO> startInterview(Authentication authentication) {
        String email = authentication.getName();
        log.info("Starting mock interview for: {}", email);

        StartInterviewResponseDTO response = interviewService.startInterview(email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{sessionId}/answer")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Submit an answer", description = "Submits the candidate's answer for evaluation. AI returns score (0-10), strengths, weaknesses, and the next question.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Answer evaluated"),
            @ApiResponse(responseCode = "400", description = "Session already ended"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<SubmitAnswerResponseDTO> submitAnswer(
            Authentication authentication,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SubmitAnswerRequestDTO request) {

        String email = authentication.getName();
        log.info("Answer submitted for session: {}", sessionId);

        SubmitAnswerResponseDTO response = interviewService.submitAnswer(sessionId, request.answer(), email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/end")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "End interview session", description = "Ends the session early and triggers AI-generated final feedback with overall score")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Final feedback returned"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<EndInterviewResponseDTO> endInterview(
            Authentication authentication,
            @PathVariable UUID sessionId) {

        String email = authentication.getName();
        log.info("Ending interview session: {}", sessionId);

        EndInterviewResponseDTO response = interviewService.endInterview(sessionId, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/transcript")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get interview transcript", description = "Returns the full Q&A transcript with per-question evaluations for a completed session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transcript returned"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<InterviewTranscriptResponseDTO> getTranscript(
            Authentication authentication,
            @PathVariable UUID sessionId) {

        String email = authentication.getName();
        log.info("Fetching transcript for session: {}", sessionId);

        InterviewTranscriptResponseDTO response = interviewService.getTranscript(sessionId, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/topic/start")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Start topic-based interview", description = "Starts a mock interview with custom topics and difficulty level (EASY, MEDIUM, HARD)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session started with first question"),
            @ApiResponse(responseCode = "400", description = "Invalid topics or difficulty")
    })
    public ResponseEntity<StartInterviewResponseDTO> startTopicInterview(
            Authentication authentication,
            @Valid @RequestBody StartTopicInterviewRequestDTO request) {

        String email = authentication.getName();
        log.info("Starting topic-based interview for: {}, Topics: {}, Difficulty: {}",
                email, request.topics(), request.difficulty());

        StartInterviewResponseDTO response = interviewService.startTopicBasedInterview(
                email, request.topics(), request.difficulty());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get interview history", description = "Lists all past completed mock interview sessions with scores and metadata")
    @ApiResponse(responseCode = "200", description = "Interview history")
    public ResponseEntity<List<InterviewHistoryDTO>> getInterviewHistory(Authentication authentication) {
        String email = authentication.getName();
        log.info("Fetching interview history for: {}", email);

        List<InterviewHistoryDTO> history = interviewService.getInterviewHistory(email);
        return ResponseEntity.ok(history);
    }
}
