package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.dto.*;
import com.codingshuttle.hackathon.skillsyncai.entity.*;
import com.codingshuttle.hackathon.skillsyncai.enums.InterviewSessionStatus;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.*;
import com.codingshuttle.hackathon.skillsyncai.service.InterviewAiService;
import com.codingshuttle.hackathon.skillsyncai.service.InterviewSessionService;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionServiceImpl implements InterviewSessionService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewTranscriptRepository transcriptRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final InterviewAiService aiService;
    private final ObjectMapper objectMapper;

    private static final int MAX_QUESTIONS = 5;

    @Override
    @Transactional(readOnly = true)
    public List<InterviewHistoryDTO> getInterviewHistory(String candidateEmail) {
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        List<InterviewSession> sessions = sessionRepository.findByCandidateIdAndStatus(
                candidate.getId(), InterviewSessionStatus.COMPLETED);

        return sessions.stream()
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .map(session -> new InterviewHistoryDTO(
                        session.getId(),
                        session.getInterviewMode() != null ? session.getInterviewMode().name() : "RESUME_BASED",
                        parseTopics(session.getTopicsJson()),
                        session.getDifficultyLevel() != null ? session.getDifficultyLevel().name() : null,
                        session.getFinalScore(),
                        session.getQuestionCount(),
                        MAX_QUESTIONS,
                        session.getStartedAt(),
                        session.getEndedAt(),
                        session.getStatus().name()))
                .toList();
    }

    @Override
    @Transactional
    public StartInterviewResponseDTO startInterview(String candidateEmail) {
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Candidate profile not found."));

        String resumeSummary = aiService.buildResumeSummary(
                user.getName(),
                candidate.getSkills(),
                candidate.getExperienceYears() != null ? candidate.getExperienceYears() : 0,
                candidate.getHeadline());

        String firstQuestion = aiService.generateFirstQuestion(resumeSummary,
                candidate.getExperienceYears() != null ? candidate.getExperienceYears() : 0);

        InterviewSession session = new InterviewSession();
        session.setCandidate(candidate);
        session.setStatus(InterviewSessionStatus.STARTED);
        session.setResumeSummary(resumeSummary);
        session.setQuestionCount(1);
        session.setInterviewMode(com.codingshuttle.hackathon.skillsyncai.enums.InterviewMode.RESUME_BASED);
        session.setTopicsJson("[]");
        session.setDifficultyLevel(com.codingshuttle.hackathon.skillsyncai.enums.DifficultyLevel.MEDIUM);
        InterviewSession savedSession = sessionRepository.save(session);

        InterviewTranscript transcript = new InterviewTranscript();
        transcript.setInterviewSession(savedSession);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("INTERVIEWER", firstQuestion));
        transcript.setMessagesJson(toJson(messages));
        transcript.setEvaluationsJson("[]");
        transcriptRepository.save(transcript);

        return new StartInterviewResponseDTO(
                savedSession.getId(),
                firstQuestion,
                MAX_QUESTIONS,
                savedSession.getInterviewMode().name(),
                savedSession.getDifficultyLevel().name());
    }

    @Override
    @Transactional
    public StartInterviewResponseDTO startTopicBasedInterview(String candidateEmail, List<String> topics,
            com.codingshuttle.hackathon.skillsyncai.enums.DifficultyLevel difficulty) {
        if (topics == null || topics.isEmpty()) {
            throw new BadRequestException("Topics list cannot be empty");
        }

        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Candidate profile not found."));

        String firstQuestion = aiService.generateFirstQuestionForTopic(topics, difficulty.name());

        InterviewSession session = new InterviewSession();
        session.setCandidate(candidate);
        session.setStatus(InterviewSessionStatus.STARTED);
        session.setInterviewMode(com.codingshuttle.hackathon.skillsyncai.enums.InterviewMode.TOPIC_BASED);
        session.setTopicsJson(toJson(topics));
        session.setDifficultyLevel(difficulty);
        session.setQuestionCount(1);
        InterviewSession savedSession = sessionRepository.save(session);

        InterviewTranscript transcript = new InterviewTranscript();
        transcript.setInterviewSession(savedSession);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("INTERVIEWER", firstQuestion));
        transcript.setMessagesJson(toJson(messages));
        transcript.setEvaluationsJson("[]");
        transcriptRepository.save(transcript);

        return new StartInterviewResponseDTO(
                savedSession.getId(),
                firstQuestion,
                MAX_QUESTIONS,
                savedSession.getInterviewMode().name(),
                savedSession.getDifficultyLevel().name());
    }

    @Override
    @Transactional
    public SubmitAnswerResponseDTO submitAnswer(UUID sessionId, String answer, String candidateEmail) {
        InterviewSession session = getAndValidateSession(sessionId, candidateEmail);

        if (session.getStatus() == InterviewSessionStatus.COMPLETED) {
            throw new BadRequestException("This interview session has already been completed.");
        }

        InterviewTranscript transcript = transcriptRepository.findByInterviewSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found"));

        List<Map<String, String>> messages = parseMessages(transcript.getMessagesJson());
        String lastQuestion = getLastInterviewerMessage(messages);
        messages.add(createMessage("CANDIDATE", answer));

        EvaluationDTO evaluation = aiService.evaluateAnswer(lastQuestion, answer, session.getResumeSummary());
        List<Map<String, Object>> evaluations = parseEvaluations(transcript.getEvaluationsJson());
        Map<String, Object> evalRecord = new HashMap<>();
        evalRecord.put("questionIndex", session.getQuestionCount());
        evalRecord.put("score", evaluation.score());
        evalRecord.put("strengths", evaluation.strengths());
        evalRecord.put("weaknesses", evaluation.weaknesses());
        evalRecord.put("feedback", evaluation.feedback());
        evaluations.add(evalRecord);

        boolean interviewComplete = session.getQuestionCount() >= MAX_QUESTIONS;
        String nextQuestion = null;

        if (!interviewComplete) {
            String performanceSummary = buildPerformanceSummary(evaluations);
            if (session.getInterviewMode() == com.codingshuttle.hackathon.skillsyncai.enums.InterviewMode.TOPIC_BASED) {
                nextQuestion = aiService.generateNextQuestionForTopic(
                        parseTopics(session.getTopicsJson()),
                        session.getDifficultyLevel() != null ? session.getDifficultyLevel().name() : "MEDIUM",
                        session.getQuestionCount() + 1,
                        performanceSummary);
            } else {
                nextQuestion = aiService.generateNextQuestion(
                        session.getResumeSummary(),
                        session.getCandidate().getExperienceYears() != null
                                ? session.getCandidate().getExperienceYears()
                                : 0,
                        session.getQuestionCount() + 1,
                        performanceSummary);
            }
            messages.add(createMessage("INTERVIEWER", nextQuestion));
            session.setQuestionCount(session.getQuestionCount() + 1);
            session.setStatus(InterviewSessionStatus.IN_PROGRESS);
        }

        transcript.setMessagesJson(toJson(messages));
        transcript.setEvaluationsJson(toJson(evaluations));
        transcriptRepository.save(transcript);
        sessionRepository.save(session);

        return new SubmitAnswerResponseDTO(evaluation, nextQuestion, interviewComplete, session.getQuestionCount());
    }

    @Override
    @Transactional
    public EndInterviewResponseDTO endInterview(UUID sessionId, String candidateEmail) {
        InterviewSession session = getAndValidateSession(sessionId, candidateEmail);

        if (session.getStatus() == InterviewSessionStatus.COMPLETED) {
            return new EndInterviewResponseDTO(
                    session.getFinalScore() != null ? session.getFinalScore() : 0.0,
                    session.getFinalFeedback() != null ? session.getFinalFeedback() : "Interview already completed.",
                    session.getQuestionCount(),
                    session.getStatus().name());
        }

        InterviewTranscript transcript = transcriptRepository.findByInterviewSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found"));

        List<Map<String, Object>> evaluations = parseEvaluations(transcript.getEvaluationsJson());
        if (evaluations.isEmpty()) {
            throw new BadRequestException("Cannot end interview without any answered questions.");
        }

        List<Integer> scores = new ArrayList<>();
        for (Map<String, Object> eval : evaluations) {
            Object scoreObj = eval.get("score");
            if (scoreObj instanceof Number) {
                scores.add(((Number) scoreObj).intValue());
            }
        }

        double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double finalScore = avgScore * 10;
        String finalFeedback = aiService.generateFinalFeedback(session.getResumeSummary(), scores);

        session.setStatus(InterviewSessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        session.setFinalScore(finalScore);
        session.setFinalFeedback(finalFeedback);
        sessionRepository.save(session);

        return new EndInterviewResponseDTO(
                finalScore,
                finalFeedback,
                session.getQuestionCount(),
                session.getStatus().name());
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewTranscriptResponseDTO getTranscript(UUID sessionId, String candidateEmail) {
        getAndValidateSession(sessionId, candidateEmail);
        InterviewTranscript transcript = transcriptRepository.findByInterviewSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transcript not found"));

        List<Map<String, String>> messageMaps = parseMessages(transcript.getMessagesJson());
        List<InterviewTranscriptResponseDTO.MessageDTO> messageDTOs = messageMaps.stream()
                .map(m -> new InterviewTranscriptResponseDTO.MessageDTO(m.get("role"), m.get("content")))
                .toList();

        List<Map<String, Object>> evaluationMaps = parseEvaluations(transcript.getEvaluationsJson());
        List<EvaluationDTO> evaluationDTOs = evaluationMaps.stream()
                .map(this::mapToEvaluationDTO)
                .toList();

        return new InterviewTranscriptResponseDTO(messageDTOs, evaluationDTOs);
    }

    @SuppressWarnings("unchecked")
    private EvaluationDTO mapToEvaluationDTO(Map<String, Object> map) {
        int score = map.get("score") instanceof Number ? ((Number) map.get("score")).intValue() : 0;
        List<String> strengths = (List<String>) map.getOrDefault("strengths", new ArrayList<>());
        List<String> weaknesses = (List<String>) map.getOrDefault("weaknesses", new ArrayList<>());
        String feedback = (String) map.getOrDefault("feedback", "Not available");
        return new EvaluationDTO(score, strengths, weaknesses, feedback);
    }

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
            if (scoreObj instanceof Number)
                totalScore += ((Number) scoreObj).intValue();
        }
        double avgScore = (double) totalScore / evaluations.size();
        if (avgScore >= 8)
            return "Strong performance";
        if (avgScore >= 5)
            return "Average performance";
        return "Needs improvement";
    }

    private List<Map<String, String>> parseMessages(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> parseEvaluations(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> parseTopics(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
