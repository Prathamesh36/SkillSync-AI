package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.*;
import com.codingshuttle.hackathon.skillsyncai.entity.*;
import com.codingshuttle.hackathon.skillsyncai.enums.InterviewSessionStatus;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for managing mock interview sessions.
 * Handles session lifecycle, transcript persistence, and AI orchestration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewTranscriptRepository transcriptRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final InterviewAiService aiService;
    private final ObjectMapper objectMapper;

    private static final int MAX_QUESTIONS = 5;

    // ================ PUBLIC METHODS ================

    /**
     * Start a new mock interview session.
     */
    @Transactional
    public StartInterviewResponseDTO startInterview(String candidateEmail) {
        log.info("Starting mock interview for candidate: {}", candidateEmail);

        // 1. Get candidate and their profile
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(
                        () -> new BadRequestException("Candidate profile not found. Please complete your profile."));

        // 2. Get latest resume for context
        Resume resume = resumeRepository.findByUserId(user.getId()).orElse(null);

        // 3. Build resume summary for AI
        String resumeSummary = aiService.buildResumeSummary(
                user.getName(),
                candidate.getSkills(),
                candidate.getExperienceYears() != null ? candidate.getExperienceYears() : 0,
                candidate.getHeadline());

        // 4. Generate first question
        int experienceYears = candidate.getExperienceYears() != null ? candidate.getExperienceYears() : 0;
        String firstQuestion = aiService.generateFirstQuestion(resumeSummary, experienceYears);

        // 5. Create session
        InterviewSession session = new InterviewSession();
        session.setCandidate(candidate);
        session.setStatus(InterviewSessionStatus.STARTED);
        session.setResumeSummary(resumeSummary);
        session.setQuestionCount(1);
        InterviewSession savedSession = sessionRepository.save(session);

        // 6. Create transcript with first question
        InterviewTranscript transcript = new InterviewTranscript();
        transcript.setInterviewSession(savedSession);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("INTERVIEWER", firstQuestion));
        transcript.setMessagesJson(toJson(messages));
        transcript.setEvaluationsJson("[]");
        transcriptRepository.save(transcript);

        log.info("Interview session started: sessionId={}", savedSession.getId());
        return new StartInterviewResponseDTO(savedSession.getId(), firstQuestion);
    }

    /**
     * Submit an answer and get the next question.
     */
    @Transactional
    public SubmitAnswerResponseDTO submitAnswer(UUID sessionId, String answer, String candidateEmail) {
        log.info("Processing answer for session: {}", sessionId);

        // 1. Validate session
        InterviewSession session = getAndValidateSession(sessionId, candidateEmail);

        if (session.getStatus() == InterviewSessionStatus.COMPLETED) {
            throw new BadRequestException("This interview session has already been completed.");
        }

        if (session.getStatus() == InterviewSessionStatus.EXPIRED) {
            throw new BadRequestException("This interview session has expired.");
        }

        // 2. Get transcript
        InterviewTranscript transcript = transcriptRepository.findByInterviewSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found"));

        // 3. Parse existing messages
        List<Map<String, String>> messages = parseMessages(transcript.getMessagesJson());

        // Get the last question asked
        String lastQuestion = getLastInterviewerMessage(messages);

        // 4. Add candidate's answer to transcript
        messages.add(createMessage("CANDIDATE", answer));

        // 5. Evaluate the answer
        EvaluationDTO evaluation = aiService.evaluateAnswer(lastQuestion, answer, session.getResumeSummary());

        // 6. Store evaluation
        List<Map<String, Object>> evaluations = parseEvaluations(transcript.getEvaluationsJson());
        Map<String, Object> evalRecord = new HashMap<>();
        evalRecord.put("questionIndex", session.getQuestionCount());
        evalRecord.put("score", evaluation.score());
        evalRecord.put("strengths", evaluation.strengths());
        evalRecord.put("weaknesses", evaluation.weaknesses());
        evaluations.add(evalRecord);

        // 7. Check if max questions reached
        boolean interviewComplete = session.getQuestionCount() >= MAX_QUESTIONS;
        String nextQuestion = null;

        if (!interviewComplete) {
            // Generate next question based on performance
            String performanceSummary = buildPerformanceSummary(evaluations);
            int experienceYears = session.getCandidate().getExperienceYears() != null
                    ? session.getCandidate().getExperienceYears()
                    : 0;

            nextQuestion = aiService.generateNextQuestion(
                    session.getResumeSummary(),
                    experienceYears,
                    session.getQuestionCount() + 1,
                    performanceSummary);

            // Add next question to transcript
            messages.add(createMessage("INTERVIEWER", nextQuestion));
            session.setQuestionCount(session.getQuestionCount() + 1);
            session.setStatus(InterviewSessionStatus.IN_PROGRESS);
        }

        // 8. Save updates
        transcript.setMessagesJson(toJson(messages));
        transcript.setEvaluationsJson(toJson(evaluations));
        transcriptRepository.save(transcript);
        sessionRepository.save(session);

        log.info("Answer processed for session: {}, questionCount={}, complete={}",
                sessionId, session.getQuestionCount(), interviewComplete);

        return new SubmitAnswerResponseDTO(evaluation, nextQuestion, interviewComplete);
    }

    /**
     * End the interview and generate final feedback.
     */
    @Transactional
    public EndInterviewResponseDTO endInterview(UUID sessionId, String candidateEmail) {
        log.info("Ending interview session: {}", sessionId);

        // 1. Validate session
        InterviewSession session = getAndValidateSession(sessionId, candidateEmail);

        if (session.getStatus() == InterviewSessionStatus.COMPLETED) {
            // Return existing results if already completed
            return new EndInterviewResponseDTO(
                    session.getFinalScore() != null ? session.getFinalScore() : 0.0,
                    session.getFinalFeedback() != null ? session.getFinalFeedback() : "Interview already completed.");
        }

        // 2. Get evaluations
        InterviewTranscript transcript = transcriptRepository.findByInterviewSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found"));

        List<Map<String, Object>> evaluations = parseEvaluations(transcript.getEvaluationsJson());

        if (evaluations.isEmpty()) {
            throw new BadRequestException("Cannot end interview without any answered questions.");
        }

        // 3. Calculate final score (average * 10 to get percentage)
        List<Integer> scores = new ArrayList<>();
        for (Map<String, Object> eval : evaluations) {
            Object scoreObj = eval.get("score");
            if (scoreObj instanceof Number) {
                scores.add(((Number) scoreObj).intValue());
            }
        }

        double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double finalScore = avgScore * 10; // Convert 0-10 to 0-100

        // 4. Generate final feedback
        String finalFeedback = aiService.generateFinalFeedback(session.getResumeSummary(), scores);

        // 5. Update session
        session.setStatus(InterviewSessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        session.setFinalScore(finalScore);
        session.setFinalFeedback(finalFeedback);
        sessionRepository.save(session);

        log.info("Interview completed: sessionId={}, finalScore={}", sessionId, finalScore);
        return new EndInterviewResponseDTO(finalScore, finalFeedback);
    }

    // ================ HELPER METHODS ================

    private InterviewSession getAndValidateSession(UUID sessionId, String candidateEmail) {
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Candidate profile not found"));

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found"));

        if (!session.getCandidate().getId().equals(candidate.getId())) {
            throw new AccessDeniedException("You are not authorized to access this interview session.");
        }

        return session;
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return message;
    }

    private String getLastInterviewerMessage(List<Map<String, String>> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("INTERVIEWER".equals(messages.get(i).get("role"))) {
                return messages.get(i).get("content");
            }
        }
        return "Tell me about yourself.";
    }

    private String buildPerformanceSummary(List<Map<String, Object>> evaluations) {
        if (evaluations.isEmpty())
            return "No previous answers";

        int totalScore = 0;
        for (Map<String, Object> eval : evaluations) {
            Object scoreObj = eval.get("score");
            if (scoreObj instanceof Number) {
                totalScore += ((Number) scoreObj).intValue();
            }
        }
        double avgScore = (double) totalScore / evaluations.size();

        if (avgScore >= 8)
            return "Strong performance (avg score: " + String.format("%.1f", avgScore) + "/10)";
        if (avgScore >= 5)
            return "Average performance (avg score: " + String.format("%.1f", avgScore) + "/10)";
        return "Needs improvement (avg score: " + String.format("%.1f", avgScore) + "/10)";
    }

    private List<Map<String, String>> parseMessages(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse messages JSON", e);
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> parseEvaluations(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse evaluations JSON", e);
            return new ArrayList<>();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize to JSON", e);
            return "[]";
        }
    }
}
