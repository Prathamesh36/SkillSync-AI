package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.CancelInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InterviewResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.RescheduleInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.ScheduleInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Application;
import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSchedule;
import com.codingshuttle.hackathon.skillsyncai.entity.Recruiter;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import com.codingshuttle.hackathon.skillsyncai.enums.InterviewScheduleStatus;
import com.codingshuttle.hackathon.skillsyncai.enums.LastUpdatedBy;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.ApplicationRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.CandidateRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.InterviewScheduleRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.RecruiterRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import com.codingshuttle.hackathon.skillsyncai.event.InterviewScheduledEvent;
import com.codingshuttle.hackathon.skillsyncai.event.InterviewRescheduledEvent;
import com.codingshuttle.hackathon.skillsyncai.event.InterviewCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for scheduling and managing real 1-on-1 interviews.
 * This is SEPARATE from the AI mock interview logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewScheduleService {

        private final InterviewScheduleRepository interviewScheduleRepository;
        private final ApplicationRepository applicationRepository;
        private final UserRepository userRepository;
        private final CandidateRepository candidateRepository;
        private final RecruiterRepository recruiterRepository;
        private final ApplicationEventPublisher eventPublisher;

        /**
         * Schedule an interview for a shortlisted candidate.
         * 
         * @param applicationId  the application to schedule interview for
         * @param request        the interview scheduling details
         * @param recruiterEmail email of the recruiter (for ownership validation)
         * @return scheduled interview details
         */
        @Transactional
        public InterviewResponseDTO scheduleInterview(Long applicationId, ScheduleInterviewRequestDTO request,
                        String recruiterEmail) {
                log.info("Scheduling interview for applicationId={}, recruiterEmail={}", applicationId, recruiterEmail);

                // 1. Get and validate application
                Application application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Application not found with id: " + applicationId));

                // 2. Validate recruiter owns the job
                if (!application.getJob().getPostedBy().getEmail().equals(recruiterEmail)) {
                        log.warn("Unauthorized schedule attempt: recruiterEmail={} tried to schedule for applicationId={}",
                                        recruiterEmail, applicationId);
                        throw new AccessDeniedException(
                                        "You are not authorized to schedule an interview for this application.");
                }

                // 3. Validate application status is SHORTLISTED
                if (application.getStatus() != ApplicationStatus.SHORTLISTED) {
                        throw new BadRequestException(
                                        "Can only schedule interviews for SHORTLISTED applications. Current status: "
                                                        + application.getStatus());
                }

                // 4. Check if interview already exists
                if (interviewScheduleRepository.existsByJobApplicationId(applicationId)) {
                        throw new BadRequestException("An interview is already scheduled for this application.");
                }

                // 5. Get recruiter entity
                User recruiterUser = userRepository.findByEmail(recruiterEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("Recruiter user not found"));
                Recruiter recruiter = recruiterRepository.findByUserId(recruiterUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

                // 6. Create interview schedule
                InterviewSchedule schedule = new InterviewSchedule();
                schedule.setJobApplication(application);
                schedule.setRecruiter(recruiter);
                schedule.setCandidate(application.getCandidate());
                schedule.setInterviewDateTime(request.interviewDateTime());
                schedule.setDurationMinutes(request.durationMinutes());
                schedule.setMode(request.mode());
                schedule.setMeetingLink(request.meetingLink());
                schedule.setStatus(InterviewScheduleStatus.SCHEDULED);
                schedule.setLastUpdatedBy(LastUpdatedBy.RECRUITER);

                InterviewSchedule saved = interviewScheduleRepository.save(schedule);

                // 7. Update application status to INTERVIEW_SCHEDULED
                application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
                applicationRepository.save(application);

                log.info("Interview scheduled: interviewId={}, applicationId={}", saved.getId(), applicationId);

                // 8. Publish event for async notification (email + calendar invite)
                eventPublisher.publishEvent(new InterviewScheduledEvent(this, saved));

                return toDTO(saved);
        }

        /**
         * Reschedule an existing interview.
         * 
         * Rules:
         * - Only SCHEDULED interviews can be rescheduled
         * - Recruiter must own the job
         * - Preserves previous date/time for audit
         * 
         * @param interviewId    the interview to reschedule
         * @param request        the new scheduling details
         * @param recruiterEmail email of the recruiter
         * @return updated interview details
         */
        @Transactional
        public InterviewResponseDTO rescheduleInterview(Long interviewId, RescheduleInterviewRequestDTO request,
                        String recruiterEmail) {
                log.info("Rescheduling interview: interviewId={}, recruiterEmail={}", interviewId, recruiterEmail);

                // 1. Fetch interview
                InterviewSchedule interview = interviewScheduleRepository.findById(interviewId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Interview not found with id: " + interviewId));

                // 2. Validate recruiter owns the job
                String jobOwnerEmail = interview.getJobApplication().getJob().getPostedBy().getEmail();
                if (!jobOwnerEmail.equals(recruiterEmail)) {
                        log.warn("Unauthorized reschedule attempt: recruiterEmail={} tried to reschedule interviewId={}",
                                        recruiterEmail, interviewId);
                        throw new AccessDeniedException("You are not authorized to reschedule this interview.");
                }

                // 3. Validate interview status - only SCHEDULED can be rescheduled
                if (interview.getStatus() != InterviewScheduleStatus.SCHEDULED) {
                        throw new BadRequestException(
                                        "Cannot reschedule interview with status: " + interview.getStatus() +
                                                        ". Only SCHEDULED interviews can be rescheduled.");
                }

                // 4. Store previous date/time for audit trail
                interview.setPreviousInterviewDateTime(interview.getInterviewDateTime());

                // 5. Update interview details
                interview.setInterviewDateTime(request.newInterviewDateTime());
                interview.setDurationMinutes(request.durationMinutes());
                if (request.meetingLink() != null) {
                        interview.setMeetingLink(request.meetingLink());
                }

                // 6. Set audit fields
                interview.setRescheduledAt(LocalDateTime.now());
                interview.setLastUpdatedBy(LastUpdatedBy.RECRUITER);
                // Status remains SCHEDULED

                InterviewSchedule saved = interviewScheduleRepository.save(interview);
                log.info("Interview rescheduled: interviewId={}, newDateTime={}", interviewId,
                                request.newInterviewDateTime());

                // 7. Publish event for async notification (updated calendar invite)
                eventPublisher.publishEvent(new InterviewRescheduledEvent(
                                this, saved, saved.getPreviousInterviewDateTime()));

                return toDTO(saved);
        }

        /**
         * Cancel an existing interview.
         * 
         * Rules:
         * - Only SCHEDULED interviews can be cancelled
         * - Recruiter must own the job
         * - Reverts JobApplication status to SHORTLISTED
         * - Preserves record for audit (no deletion)
         * 
         * @param interviewId    the interview to cancel
         * @param request        contains cancellation reason
         * @param recruiterEmail email of the recruiter
         * @return updated interview details
         */
        @Transactional
        public InterviewResponseDTO cancelInterview(Long interviewId, CancelInterviewRequestDTO request,
                        String recruiterEmail) {
                log.info("Cancelling interview: interviewId={}, recruiterEmail={}", interviewId, recruiterEmail);

                // 1. Fetch interview
                InterviewSchedule interview = interviewScheduleRepository.findById(interviewId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Interview not found with id: " + interviewId));

                // 2. Validate recruiter owns the job
                String jobOwnerEmail = interview.getJobApplication().getJob().getPostedBy().getEmail();
                if (!jobOwnerEmail.equals(recruiterEmail)) {
                        log.warn("Unauthorized cancel attempt: recruiterEmail={} tried to cancel interviewId={}",
                                        recruiterEmail, interviewId);
                        throw new AccessDeniedException("You are not authorized to cancel this interview.");
                }

                // 3. Validate interview status - only SCHEDULED can be cancelled
                if (interview.getStatus() != InterviewScheduleStatus.SCHEDULED) {
                        throw new BadRequestException(
                                        "Cannot cancel interview with status: " + interview.getStatus() +
                                                        ". Only SCHEDULED interviews can be cancelled.");
                }

                // 4. Update interview status and audit fields
                interview.setStatus(InterviewScheduleStatus.CANCELLED);
                interview.setCancelledAt(LocalDateTime.now());
                interview.setCancellationReason(request.reason());
                interview.setLastUpdatedBy(LastUpdatedBy.RECRUITER);

                InterviewSchedule saved = interviewScheduleRepository.save(interview);

                // 5. Revert JobApplication status to SHORTLISTED
                Application application = interview.getJobApplication();
                application.setStatus(ApplicationStatus.SHORTLISTED);
                applicationRepository.save(application);

                log.info("Interview cancelled: interviewId={}, reason={}, applicationStatus reverted to SHORTLISTED",
                                interviewId, request.reason());

                // 6. Publish event for async notification (cancellation calendar invite)
                eventPublisher.publishEvent(new InterviewCancelledEvent(this, saved, request.reason()));

                return toDTO(saved);
        }

        /**
         * Get all interviews for the currently logged-in candidate.
         */
        public List<InterviewResponseDTO> getCandidateInterviews(String candidateEmail) {
                log.debug("Fetching interviews for candidate: {}", candidateEmail);

                User user = userRepository.findByEmail(candidateEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found with email: " + candidateEmail));

                Candidate candidate = candidateRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new BadRequestException("Candidate profile not found."));

                return interviewScheduleRepository.findByCandidateId(candidate.getId()).stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Get all interviews for a job (Recruiter only).
         * Validates that the recruiter owns the job.
         */
        public List<InterviewResponseDTO> getRecruiterInterviewsForJob(Long jobId, String recruiterEmail) {
                log.debug("Fetching interviews for jobId={}, recruiterEmail={}", jobId, recruiterEmail);

                // Get all interviews for the job
                List<InterviewSchedule> interviews = interviewScheduleRepository.findByJobApplicationJobId(jobId);

                // If there are interviews, validate recruiter owns the job
                if (!interviews.isEmpty()) {
                        Application firstApp = interviews.get(0).getJobApplication();
                        if (!firstApp.getJob().getPostedBy().getEmail().equals(recruiterEmail)) {
                                log.warn("Unauthorized access attempt: recruiterEmail={} tried to view interviews for jobId={}",
                                                recruiterEmail, jobId);
                                throw new AccessDeniedException(
                                                "You are not authorized to view interviews for this job.");
                        }
                }

                return interviews.stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Get all interviews for a verified recruiter.
         */
        public List<InterviewResponseDTO> getInterviewsForRecruiter(String recruiterEmail) {
                log.debug("Fetching interviews for recruiter: {}", recruiterEmail);

                User user = userRepository.findByEmail(recruiterEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found with email: " + recruiterEmail));

                Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

                return interviewScheduleRepository.findByRecruiterId(recruiter.getId()).stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Convert InterviewSchedule entity to DTO.
         */
        private InterviewResponseDTO toDTO(InterviewSchedule schedule) {
                return new InterviewResponseDTO(
                                schedule.getId(),
                                schedule.getJobApplication().getId(),
                                schedule.getJobApplication().getJob().getTitle(),
                                schedule.getJobApplication().getJob().getCompanyName(),
                                schedule.getCandidate().getUser().getName(),
                                schedule.getRecruiter().getUser().getName(),
                                schedule.getInterviewDateTime(),
                                schedule.getDurationMinutes(),
                                schedule.getMode(),
                                schedule.getMeetingLink(),
                                schedule.getStatus());
        }
}
