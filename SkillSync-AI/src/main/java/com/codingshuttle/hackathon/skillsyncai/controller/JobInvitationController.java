package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.InvitationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InviteCandidateRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InvitationAcceptResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.service.JobInvitationService;
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
public class JobInvitationController {

        private final JobInvitationService jobInvitationService;

        /**
         * Recruiter invites a candidate to apply for a job.
         */
        @PostMapping("/jobs/{jobId}/invite")
        @PreAuthorize("hasRole('RECRUITER')")
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

        /**
         * Candidate views all their invitations.
         */
        @GetMapping("/candidates/me/invitations")
        @PreAuthorize("hasRole('CANDIDATE')")
        public ResponseEntity<List<InvitationResponseDTO>> getCandidateInvitations(
                        Authentication authentication) {

                String candidateEmail = authentication.getName();
                List<InvitationResponseDTO> invitations = jobInvitationService
                                .getCandidateInvitations(candidateEmail);

                return ResponseEntity.ok(invitations);
        }

        /**
         * Candidate accepts an invitation using the secure token.
         * Creates a JobApplication.
         */
        @PostMapping("/invitations/{token}/accept")
        @PreAuthorize("hasRole('CANDIDATE')")
        public ResponseEntity<InvitationAcceptResponseDTO> acceptInvitation(
                        Authentication authentication,
                        @PathVariable String token) {

                String candidateEmail = authentication.getName();
                log.info("Accept invitation: token={}, candidate={}", token, candidateEmail);

                InvitationAcceptResponseDTO response = jobInvitationService
                                .acceptInvitation(token, candidateEmail);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Candidate declines an invitation using the secure token.
         */
        @PostMapping("/invitations/{token}/decline")
        @PreAuthorize("hasRole('CANDIDATE')")
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
