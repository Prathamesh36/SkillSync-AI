package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.*;
import com.codingshuttle.hackathon.skillsyncai.enums.DifficultyLevel;

import java.util.List;
import java.util.UUID;

public interface InterviewSessionService {
    List<InterviewHistoryDTO> getInterviewHistory(String candidateEmail);

    StartInterviewResponseDTO startInterview(String candidateEmail);

    StartInterviewResponseDTO startTopicBasedInterview(String candidateEmail, List<String> topics,
            DifficultyLevel difficulty);

    SubmitAnswerResponseDTO submitAnswer(UUID sessionId, String answer, String candidateEmail);

    EndInterviewResponseDTO endInterview(UUID sessionId, String candidateEmail);

    InterviewTranscriptResponseDTO getTranscript(UUID sessionId, String candidateEmail);
}
