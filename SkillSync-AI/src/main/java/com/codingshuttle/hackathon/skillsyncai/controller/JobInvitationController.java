package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.InvitationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InviteCandidateRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InvitationAcceptResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.service.JobInvitationService;
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

/**
 * Controller for job invitation operations.
 * 
 * Endpoints:
 * - POST /jobs/{jobId}/invite - Recruiter invites candidate
 * - GET /candidates/me/invitations - Candidate views invitations
 * - POST /invitations/{token}/accept - Candidate accepts
 * - POST /invitations/{token}/decline - Candidate declines
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job Invitations", description = "Recruiter-to-candidate job invitations with token-based accept/decline")
public class JobInvitationController {

        private final JobInvitationService jobInvitationService;

        @PostMapping("/jobs/{jobId}/invite")
        @PreAuthorize("hasRole('RECRUITER')")
        @Operation(summary = "Invite a candidate", description = "Sends a job invitation to a matched candidate with an optional personalized message. Triggers email notification.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Invitation sent"),
                        @ApiResponse(responseCode = "400", description = "Candidate already invited or applied"),
                        @ApiResponse(responseCode = "404", description = "Job or candidate not found")
        })
        public ResponseEntity<InvitationResponseDTO> inviteCandidate(
                        Authentication authentication,
                        @PathVariable Long jobId,
                        @Valid @RequestBody InviteCandidateRequestDTO request) {

                String recruiterEmail = authentication.getName();
                log.info("Invite request: jobId={}, candidateId={}, recruiter={}",
                                jobId, request.candidateId(), recruiterEmail);

                InvitationResponseDTO response = jobInvitationService.inviteCandidate(
                                jobId, request, recruiterEmail);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping("/candidates/me/invitations")
        @PreAuthorize("hasRole('CANDIDATE')")
        @Operation(summary = "Get my invitations", description = "Lists all job invitations received by the current candidate")
        @ApiResponse(responseCode = "200", description = "List of invitations")
        public ResponseEntity<List<InvitationResponseDTO>> getCandidateInvitations(
                        Authentication authentication) {

                String candidateEmail = authentication.getName();
                List<InvitationResponseDTO> invitations = jobInvitationService
                                .getCandidateInvitations(candidateEmail);

                return ResponseEntity.ok(invitations);
        }

        @PostMapping("/invitations/{token}/accept")
        @PreAuthorize("hasRole('CANDIDATE')")
        @Operation(summary = "Accept an invitation", description = "Accepts a job invitation using the secure token. Automatically creates a job application.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Invitation accepted, application created"),
                        @ApiResponse(responseCode = "400", description = "Invitation expired or already responded"),
                        @ApiResponse(responseCode = "404", description = "Invalid token")
        })
        public ResponseEntity<InvitationAcceptResponseDTO> acceptInvitation(
                        Authentication authentication,
                        @PathVariable String token) {

                String candidateEmail = authentication.getName();
                log.info("Accept invitation: token={}, candidate={}", token, candidateEmail);

                InvitationAcceptResponseDTO response = jobInvitationService
                                .acceptInvitation(token, candidateEmail);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PostMapping("/invitations/{token}/decline")
        @PreAuthorize("hasRole('CANDIDATE')")
        @Operation(summary = "Decline an invitation", description = "Declines a job invitation using the secure token")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Invitation declined"),
                        @ApiResponse(responseCode = "400", description = "Invitation expired or already responded"),
                        @ApiResponse(responseCode = "404", description = "Invalid token")
        })
        public ResponseEntity<InvitationResponseDTO> declineInvitation(
                        Authentication authentication,
                        @PathVariable String token) {

                String candidateEmail = authentication.getName();
                log.info("Decline invitation: token={}, candidate={}", token, candidateEmail);

                InvitationResponseDTO response = jobInvitationService
                                .declineInvitation(token, candidateEmail);

                return ResponseEntity.ok(response);
        }
}
