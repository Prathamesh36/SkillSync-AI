package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.dto.InvitationAcceptResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InvitationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InviteCandidateRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.*;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import com.codingshuttle.hackathon.skillsyncai.enums.JobInvitationStatus;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.*;
import com.codingshuttle.hackathon.skillsyncai.service.JobInvitationService;
import com.codingshuttle.hackathon.skillsyncai.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobInvitationServiceImpl implements JobInvitationService {

        private final JobInvitationRepository jobInvitationRepository;
        private final JobRepository jobRepository;
        private final CandidateRepository candidateRepository;
        private final RecruiterRepository recruiterRepository;
        private final UserRepository userRepository;
        private final ApplicationRepository applicationRepository;
        private final ResumeRepository resumeRepository;
        private final NotificationService notificationService;

        private static final int INVITATION_EXPIRY_DAYS = 7;

        @Override
        @Transactional
        public InvitationResponseDTO inviteCandidate(Long jobId, InviteCandidateRequestDTO request,
                        String recruiterEmail) {
                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
                if (!job.getPostedBy().getEmail().equals(recruiterEmail)) {
                        throw new AccessDeniedException("You are not authorized to invite candidates for this job.");
                }

                User recruiterUser = userRepository.findByEmail(recruiterEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("Recruiter user not found"));
                Recruiter recruiter = recruiterRepository.findByUserId(recruiterUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

                Candidate candidate = candidateRepository.findById(request.candidateId())
                                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

                if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
                        throw new BadRequestException("Candidate has already applied for this job.");
                }

                if (jobInvitationRepository.existsByJobIdAndCandidateIdAndStatus(jobId, candidate.getId(),
                                JobInvitationStatus.SENT)) {
                        throw new BadRequestException("An active invitation already exists.");
                }

                JobInvitation invitation = new JobInvitation();
                invitation.setJob(job);
                invitation.setCandidate(candidate);
                invitation.setInvitedBy(recruiter);
                invitation.setStatus(JobInvitationStatus.SENT);
                invitation.setInvitedAt(LocalDateTime.now());
                invitation.setMessage(request.message());
                invitation.setInvitationToken(UUID.randomUUID().toString());
                invitation.setExpiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS));

                JobInvitation saved = jobInvitationRepository.save(invitation);
                notificationService.sendJobInvitationEmail(saved);

                return toDTO(saved);
        }

        @Override
        public List<InvitationResponseDTO> getCandidateInvitations(String candidateEmail) {
                User user = userRepository.findByEmail(candidateEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                Candidate candidate = candidateRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

                List<JobInvitation> invitations = jobInvitationRepository
                                .findByCandidateIdOrderByInvitedAtDesc(candidate.getId());
                invitations.forEach(inv -> {
                        if (inv.getStatus() == JobInvitationStatus.SENT && inv.isExpired()) {
                                inv.setStatus(JobInvitationStatus.EXPIRED);
                                jobInvitationRepository.save(inv);
                        }
                });

                return invitations.stream().map(this::toDTO).collect(Collectors.toList());
        }

        @Override
        @Transactional
        public InvitationAcceptResponseDTO acceptInvitation(String token, String candidateEmail) {
                JobInvitation invitation = jobInvitationRepository.findByInvitationToken(token)
                                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token."));
                User user = userRepository.findByEmail(candidateEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                Candidate candidate = candidateRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

                if (!invitation.getCandidate().getId().equals(candidate.getId())) {
                        throw new AccessDeniedException("This invitation does not belong to you.");
                }

                if (invitation.getStatus() != JobInvitationStatus.SENT) {
                        throw new BadRequestException(
                                        "This invitation has already been "
                                                        + invitation.getStatus().name().toLowerCase() + ".");
                }

                if (invitation.isExpired()) {
                        invitation.setStatus(JobInvitationStatus.EXPIRED);
                        jobInvitationRepository.save(invitation);
                        throw new BadRequestException("This invitation has expired.");
                }

                Resume resume = resumeRepository.findFirstByUserIdOrderByIdDesc(user.getId())
                                .orElseThrow(() -> new BadRequestException("Please upload a resume first."));

                Application application = new Application();
                application.setJob(invitation.getJob());
                application.setCandidate(candidate);
                application.setResume(resume);
                application.setStatus(ApplicationStatus.APPLIED);

                Application savedApp = applicationRepository.save(application);
                invitation.setStatus(JobInvitationStatus.ACCEPTED);
                invitation.setRespondedAt(LocalDateTime.now());
                jobInvitationRepository.save(invitation);

                return new InvitationAcceptResponseDTO(
                                invitation.getId(),
                                "ACCEPTED",
                                invitation.getJob().getId(),
                                invitation.getJob().getTitle(),
                                invitation.getJob().getCompanyName(),
                                savedApp.getId(),
                                savedApp.getStatus().name(),
                                "Application created successfully.");
        }

        @Override
        @Transactional
        public InvitationResponseDTO declineInvitation(String token, String candidateEmail) {
                JobInvitation invitation = jobInvitationRepository.findByInvitationToken(token)
                                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token."));
                User user = userRepository.findByEmail(candidateEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                Candidate candidate = candidateRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

                if (!invitation.getCandidate().getId().equals(candidate.getId())) {
                        throw new AccessDeniedException("This invitation does not belong to you.");
                }

                if (invitation.getStatus() != JobInvitationStatus.SENT) {
                        throw new BadRequestException(
                                        "This invitation has already been "
                                                        + invitation.getStatus().name().toLowerCase() + ".");
                }

                invitation.setStatus(JobInvitationStatus.DECLINED);
                invitation.setRespondedAt(LocalDateTime.now());
                return toDTO(jobInvitationRepository.save(invitation));
        }

        private InvitationResponseDTO toDTO(JobInvitation invitation) {
                return new InvitationResponseDTO(
                                invitation.getId(),
                                invitation.getJob().getId(),
                                invitation.getJob().getTitle(),
                                invitation.getJob().getCompanyName(),
                                invitation.getInvitedBy().getUser().getName(),
                                invitation.getInvitedBy().getUser().getEmail(),
                                invitation.getMessage(),
                                invitation.getStatus(),
                                invitation.getInvitationToken(),
                                invitation.getInvitedAt(),
                                invitation.getExpiresAt(),
                                invitation.getCandidate().getId(),
                                invitation.getCandidate().getUser().getName());
        }
}
