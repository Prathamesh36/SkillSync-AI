package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.CancelInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InterviewResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.RescheduleInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.ScheduleInterviewRequestDTO;

import java.util.List;

public interface InterviewScheduleService {
        InterviewResponseDTO scheduleInterview(Long applicationId, ScheduleInterviewRequestDTO request,
                        String recruiterEmail);

        InterviewResponseDTO rescheduleInterview(Long interviewId, RescheduleInterviewRequestDTO request,
                        String recruiterEmail);

        InterviewResponseDTO cancelInterview(Long interviewId, CancelInterviewRequestDTO request,
                        String recruiterEmail);

        List<InterviewResponseDTO> getCandidateInterviews(String candidateEmail);

        List<InterviewResponseDTO> getRecruiterInterviewsForJob(Long jobId, String recruiterEmail);

        List<InterviewResponseDTO> getInterviewsForRecruiter(String recruiterEmail);
}
