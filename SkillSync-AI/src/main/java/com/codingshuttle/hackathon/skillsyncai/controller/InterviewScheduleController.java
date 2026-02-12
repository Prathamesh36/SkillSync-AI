package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.CancelInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InterviewResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.RescheduleInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.service.InterviewScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing scheduled interviews.
 * Provides endpoints for reschedule and cancel operations.
 * 
 * Note: Schedule and view endpoints are in ApplicationController
 * to maintain logical grouping with applications.
 */
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Interview Scheduling", description = "Reschedule and cancel scheduled interviews")
public class InterviewScheduleController {

        private final InterviewScheduleService interviewScheduleService;

        @PatchMapping("/{interviewId}/reschedule")
        @PreAuthorize("hasRole('RECRUITER')")
        @Operation(summary = "Reschedule an interview", description = "Changes date/time and optionally meeting link. Sends updated email notification. Interview must be in SCHEDULED status.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Interview rescheduled"),
                        @ApiResponse(responseCode = "400", description = "Interview not in SCHEDULED status"),
                        @ApiResponse(responseCode = "403", description = "Not the job owner"),
                        @ApiResponse(responseCode = "404", description = "Interview not found")
        })
        public ResponseEntity<InterviewResponseDTO> rescheduleInterview(
                        Authentication authentication,
                        @PathVariable Long interviewId,
                        @Valid @RequestBody RescheduleInterviewRequestDTO request) {

                String recruiterEmail = authentication.getName();
                log.info("Reschedule request: interviewId={}, recruiter={}, newDateTime={}",
                                interviewId, recruiterEmail, request.newInterviewDateTime());

                InterviewResponseDTO response = interviewScheduleService.rescheduleInterview(
                                interviewId, request, recruiterEmail);

                return ResponseEntity.ok(response);
        }

        @PatchMapping("/{interviewId}/cancel")
        @PreAuthorize("hasRole('RECRUITER')")
        @Operation(summary = "Cancel an interview", description = "Cancels a scheduled interview with a reason. Reverts the application status to SHORTLISTED.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Interview cancelled"),
                        @ApiResponse(responseCode = "400", description = "Interview not in SCHEDULED status"),
                        @ApiResponse(responseCode = "403", description = "Not the job owner"),
                        @ApiResponse(responseCode = "404", description = "Interview not found")
        })
        public ResponseEntity<InterviewResponseDTO> cancelInterview(
                        Authentication authentication,
                        @PathVariable Long interviewId,
                        @Valid @RequestBody CancelInterviewRequestDTO request) {

                String recruiterEmail = authentication.getName();
                log.info("Cancel request: interviewId={}, recruiter={}, reason={}",
                                interviewId, recruiterEmail, request.reason());

                InterviewResponseDTO response = interviewScheduleService.cancelInterview(
                                interviewId, request, recruiterEmail);

                return ResponseEntity.ok(response);
        }
}
