package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.InvitationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InviteCandidateRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InvitationAcceptResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.*;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import com.codingshuttle.hackathon.skillsyncai.enums.JobInvitationStatus;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing job invitations.
 * 
 * Key responsibilities:
 * - Recruiter invites candidate to apply
 * - Candidate views/accepts/declines invitations
 * - Creates JobApplication upon acceptance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobInvitationService {

        private final JobInvitationRepository jobInvitationRepository;
        private final JobRepository jobRepository;
        private final CandidateRepository candidateRepository;
        private final RecruiterRepository recruiterRepository;
        private final UserRepository userRepository;
        private final ApplicationRepository applicationRepository;
        private final ResumeRepository resumeRepository;
        private final NotificationService notificationService;

        /** Invitation validity period in days */
        private static final int INVITATION_EXPIRY_DAYS = 7;

        // ======================== RECRUITER OPERATIONS ========================

        /**
         * Invite a candidate to apply for a job.
         * 
         * Business Rules:
         * - Recruiter must own the job
         * - Candidate must exist
         * - No existing JobApplication for this job+candidate
         * - No active (SENT) invitation for this job+candidate
         * 
         * @param jobId          the job to invite for
         * @param request        contains candidateId and optional message
         * @param recruiterEmail email of the inviting recruiter
         * @return created invitation details
         */
        @Transactional
        public InvitationResponseDTO inviteCandidate(Long jobId, InviteCandidateRequestDTO request,
                        String recruiterEmail) {
                log.info("Inviting candidate: jobId={}, candidateId={}, recruiterEmail={}",
                                jobId, request.candidateId(), recruiterEmail);

                // 1. Validate job exists
                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

                // 2. Validate recruiter owns the job
                if (!job.getPostedBy().getEmail().equals(recruiterEmail)) {
                        log.warn("Unauthorized invite: recruiterEmail={} does not own jobId={}", recruiterEmail, jobId);
                        throw new AccessDeniedException("You are not authorized to invite candidates for this job.");
                }

                // 3. Get recruiter entity
                User recruiterUser = userRepository.findByEmail(recruiterEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("Recruiter user not found"));
                Recruiter recruiter = recruiterRepository.findByUserId(recruiterUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

                // 4. Validate candidate exists
                Candidate candidate = candidateRepository.findById(request.candidateId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Candidate not found with id: " + request.candidateId()));

                // 5. Check if candidate already has a JobApplication for this job
                if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
                        throw new BadRequestException("Candidate has already applied for this job.");
                }

                // 6. Check for active (SENT) invitation
                if (jobInvitationRepository.existsByJobIdAndCandidateIdAndStatus(
                                jobId, candidate.getId(), JobInvitationStatus.SENT)) {
                        throw new BadRequestException("An active invitation already exists for this candidate.");
                }

                // 7. Create invitation
                JobInvitation invitation = new JobInvitation();
                invitation.setJob(job);
                invitation.setCandidate(candidate);
                invitation.setInvitedBy(recruiter);
                invitation.setStatus(JobInvitationStatus.SENT);
                invitation.setInvitedAt(LocalDateTime.now());
                invitation.setMessage(request.message());
                invitation.setInvitationToken(generateSecureToken());
                invitation.setExpiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS));

                JobInvitation saved = jobInvitationRepository.save(invitation);
                log.info("Invitation created: invitationId={}, token={}", saved.getId(), saved.getInvitationToken());

                // 8. Send email notification
                notificationService.sendJobInvitationEmail(saved);

                return toDTO(saved);
        }

        // ======================== CANDIDATE OPERATIONS ========================

        /**
         * Get all invitations for the logged-in candidate.
         */
        public List<InvitationResponseDTO> getCandidateInvitations(String candidateEmail) {
                log.debug("Fetching invitations for candidate: {}", candidateEmail);

                User user = userRepository.findByEmail(candidateEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + candidateEmail));

                Candidate candidate = candidateRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

                List<JobInvitation> invitations = jobInvitationRepository
                                .findByCandidateIdOrderByInvitedAtDesc(candidate.getId());

                // Mark expired invitations
                invitations.forEach(inv -> {
                        if (inv.getStatus() == JobInvitationStatus.SENT && inv.isExpired()) {
                                inv.setStatus(JobInvitationStatus.EXPIRED);
                                jobInvitationRepository.save(inv);
                        }
                });

                return invitations.stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Accept an invitation using the secure token.
         * Creates a JobApplication with status APPLIED.
         * 
         * @param token          the invitation token
         * @param candidateEmail email of the accepting candidate (for validation)
         * @return the created application ID
         */
        @Transactional
        public InvitationAcceptResponseDTO acceptInvitation(String token, String candidateEmail) {
                log.info("Accepting invitation: token={}, candidateEmail={}", token, candidateEmail);

                // 1. Find invitation by token
                JobInvitation invitation = jobInvitationRepository.findByInvitationToken(token)
                                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token."));

                // 2. Validate invitation belongs to this candidate
                User user = userRepository.findByEmail(candidateEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                Candidate candidate = candidateRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

                if (!invitation.getCandidate().getId().equals(candidate.getId())) {
                        log.warn("Token ownership mismatch: token={} does not belong to candidateId={}",
                                        token, candidate.getId());
                        throw new AccessDeniedException("This invitation does not belong to you.");
                }

                // 3. Validate invitation status
                if (invitation.getStatus() != JobInvitationStatus.SENT) {
                        throw new BadRequestException("This invitation has already been " +
                                        invitation.getStatus().name().toLowerCase() + ".");
                }

                // 4. Validate not expired
                if (invitation.isExpired()) {
                        invitation.setStatus(JobInvitationStatus.EXPIRED);
                        jobInvitationRepository.save(invitation);
                        throw new BadRequestException("This invitation has expired.");
                }

                // 5. Create JobApplication
                // Get candidate's latest resume (or any resume)
                Resume resume = resumeRepository.findFirstByUserIdOrderByIdDesc(user.getId())
                                .orElseThrow(() -> new BadRequestException(
                                                "Please upload a resume before accepting the invitation."));

                Application application = new Application();
                application.setJob(invitation.getJob());
                application.setCandidate(candidate);
                application.setResume(resume);
                application.setStatus(ApplicationStatus.APPLIED);
                // No coverLetter for invited applications (could be enhanced)

                Application savedApp = applicationRepository.save(application);
                log.info("JobApplication created from invitation: applicationId={}, invitationId={}",
                                savedApp.getId(), invitation.getId());

                // 6. Update invitation status
                invitation.setStatus(JobInvitationStatus.ACCEPTED);
                invitation.setRespondedAt(LocalDateTime.now());
                jobInvitationRepository.save(invitation);

                return new InvitationAcceptResponseDTO(
                                savedApp.getId(),
                                invitation.getJob().getId(),
                                invitation.getJob().getTitle(),
                                savedApp.getStatus(),
                                "Application created successfully from invitation.");
        }

        /**
         * Decline an invitation using the secure token.
         */
        @Transactional
        public InvitationResponseDTO declineInvitation(String token, String candidateEmail) {
                log.info("Declining invitation: token={}, candidateEmail={}", token, candidateEmail);

                // 1. Find invitation by token
                JobInvitation invitation = jobInvitationRepository.findByInvitationToken(token)
                                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token."));

                // 2. Validate invitation belongs to this candidate
                User user = userRepository.findByEmail(candidateEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                Candidate candidate = candidateRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

                if (!invitation.getCandidate().getId().equals(candidate.getId())) {
                        throw new AccessDeniedException("This invitation does not belong to you.");
                }

                // 3. Validate invitation status
                if (invitation.getStatus() != JobInvitationStatus.SENT) {
                        throw new BadRequestException("This invitation has already been " +
                                        invitation.getStatus().name().toLowerCase() + ".");
                }

                // 4. Update status
                invitation.setStatus(JobInvitationStatus.DECLINED);
                invitation.setRespondedAt(LocalDateTime.now());
                JobInvitation saved = jobInvitationRepository.save(invitation);

                log.info("Invitation declined: invitationId={}", saved.getId());
                return toDTO(saved);
        }

        // ======================== HELPER METHODS ========================

        /**
         * Generate a secure, unique token for the invitation.
         */
        private String generateSecureToken() {
                return UUID.randomUUID().toString();
        }

        /**
         * Convert entity to DTO.
         */
        private InvitationResponseDTO toDTO(JobInvitation invitation) {
                return new InvitationResponseDTO(
                                invitation.getId(),
                                invitation.getJob().getId(),
                                invitation.getJob().getTitle(),
                                invitation.getJob().getCompanyName(),
                                invitation.getInvitedBy().getUser().getName(),
                                invitation.getMessage(),
                                invitation.getStatus(),
                                invitation.getInvitedAt(),
                                invitation.getExpiresAt(),
                                invitation.getRespondedAt());
        }
}
