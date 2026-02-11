package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.EvaluationDTO;

import java.util.List;

public interface InterviewAiService {
        String generateFirstQuestion(String resumeSummary, int experienceYears);

        String generateNextQuestion(String resumeSummary, int experienceYears, int questionNumber,
                        String performanceSummary);

        EvaluationDTO evaluateAnswer(String question, String answer, String resumeSummary);

        String generateFinalFeedback(String resumeSummary, List<Integer> scores);

        String generateFirstQuestionForTopic(List<String> topics, String difficulty);

        String generateNextQuestionForTopic(List<String> topics, String difficulty, int questionNumber,
                        String performanceSummary);

        String buildResumeSummary(String candidateName, List<String> skills, int experienceYears, String headline);
}
