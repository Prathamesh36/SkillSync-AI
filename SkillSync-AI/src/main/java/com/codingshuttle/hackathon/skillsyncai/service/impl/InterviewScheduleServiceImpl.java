package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.dto.CancelInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InterviewResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.RescheduleInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.ScheduleInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.*;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import com.codingshuttle.hackathon.skillsyncai.enums.InterviewScheduleStatus;
import com.codingshuttle.hackathon.skillsyncai.enums.LastUpdatedBy;
import com.codingshuttle.hackathon.skillsyncai.event.InterviewCancelledEvent;
import com.codingshuttle.hackathon.skillsyncai.event.InterviewRescheduledEvent;
import com.codingshuttle.hackathon.skillsyncai.event.InterviewScheduledEvent;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.*;
import com.codingshuttle.hackathon.skillsyncai.service.InterviewScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewScheduleServiceImpl implements InterviewScheduleService {

    private final InterviewScheduleRepository interviewScheduleRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final RecruiterRepository recruiterRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public InterviewResponseDTO scheduleInterview(Long applicationId, ScheduleInterviewRequestDTO request,
            String recruiterEmail) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getJob().getPostedBy().getEmail().equals(recruiterEmail))
            throw new AccessDeniedException("Unauthorized");
        if (application.getStatus() != ApplicationStatus.SHORTLISTED)
            throw new BadRequestException("Application not shortlisted");
        if (interviewScheduleRepository.existsByJobApplicationId(applicationId))
            throw new BadRequestException("Interview already scheduled");

        User recruiterUser = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

        InterviewSchedule schedule = new InterviewSchedule();
        schedule.setJobApplication(application);
        schedule.setRecruiter(recruiter);
        schedule.setCandidate(application.getCandidate());
        schedule.setInterviewDateTime(request.interviewDateTime());
        schedule.setDurationMinutes(request.durationMinutes());
        schedule.setMode(request.interviewMode());
        schedule.setMeetingLink(request.meetingLink());
        schedule.setStatus(InterviewScheduleStatus.SCHEDULED);
        schedule.setLastUpdatedBy(LastUpdatedBy.RECRUITER);

        InterviewSchedule saved = interviewScheduleRepository.save(schedule);
        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);

        eventPublisher.publishEvent(new InterviewScheduledEvent(this, saved));
        return toDTO(saved);
    }

    @Override
    @Transactional
    public InterviewResponseDTO rescheduleInterview(Long interviewId, RescheduleInterviewRequestDTO request,
            String recruiterEmail) {
        InterviewSchedule interview = interviewScheduleRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
        if (!interview.getJobApplication().getJob().getPostedBy().getEmail().equals(recruiterEmail))
            throw new AccessDeniedException("Unauthorized");
        if (interview.getStatus() != InterviewScheduleStatus.SCHEDULED)
            throw new BadRequestException("Can only reschedule if scheduled");

        interview.setPreviousInterviewDateTime(interview.getInterviewDateTime());
        interview.setInterviewDateTime(request.newInterviewDateTime());
        interview.setDurationMinutes(request.durationMinutes());
        if (request.meetingLink() != null)
            interview.setMeetingLink(request.meetingLink());
        interview.setRescheduledAt(LocalDateTime.now());
        interview.setLastUpdatedBy(LastUpdatedBy.RECRUITER);

        InterviewSchedule saved = interviewScheduleRepository.save(interview);
        eventPublisher.publishEvent(new InterviewRescheduledEvent(this, saved, saved.getPreviousInterviewDateTime()));
        return toDTO(saved);
    }

    @Override
    @Transactional
    public InterviewResponseDTO cancelInterview(Long interviewId, CancelInterviewRequestDTO request,
            String recruiterEmail) {
        InterviewSchedule interview = interviewScheduleRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
        if (!interview.getJobApplication().getJob().getPostedBy().getEmail().equals(recruiterEmail))
            throw new AccessDeniedException("Unauthorized");
        if (interview.getStatus() != InterviewScheduleStatus.SCHEDULED)
            throw new BadRequestException("Can only cancel if scheduled");

        interview.setStatus(InterviewScheduleStatus.CANCELLED);
        interview.setCancelledAt(LocalDateTime.now());
        interview.setCancellationReason(request.reason());
        interview.setLastUpdatedBy(LastUpdatedBy.RECRUITER);

        interview.getJobApplication().setStatus(ApplicationStatus.SHORTLISTED);
        applicationRepository.save(interview.getJobApplication());

        InterviewSchedule saved = interviewScheduleRepository.save(interview);
        eventPublisher.publishEvent(new InterviewCancelledEvent(this, saved, request.reason()));
        return toDTO(saved);
    }

    @Override
    public List<InterviewResponseDTO> getCandidateInterviews(String candidateEmail) {
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Profile not found"));
        return interviewScheduleRepository.findByCandidateId(candidate.getId()).stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InterviewResponseDTO> getRecruiterInterviewsForJob(Long jobId, String recruiterEmail) {
        List<InterviewSchedule> interviews = interviewScheduleRepository.findByJobApplicationJobId(jobId);
        if (!interviews.isEmpty()) {
            if (!interviews.get(0).getJobApplication().getJob().getPostedBy().getEmail().equals(recruiterEmail))
                throw new AccessDeniedException("Unauthorized");
        }
        return interviews.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<InterviewResponseDTO> getInterviewsForRecruiter(String recruiterEmail) {
        User user = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return interviewScheduleRepository.findByRecruiterId(recruiter.getId()).stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    private InterviewResponseDTO toDTO(InterviewSchedule s) {
        return new InterviewResponseDTO(s.getId(),
                s.getJobApplication().getId(),
                s.getJobApplication().getJob().getId(),
                s.getJobApplication().getJob().getTitle(),
                s.getJobApplication().getJob().getCompanyName(),
                s.getCandidate().getUser().getName(),
                s.getCandidate().getUser().getEmail(),
                s.getRecruiter().getUser().getName(),
                s.getRecruiter().getUser().getEmail(),
                s.getInterviewDateTime(),
                s.getDurationMinutes(),
                s.getMode(),
                s.getMeetingLink(),
                s.getStatus());
    }
}
