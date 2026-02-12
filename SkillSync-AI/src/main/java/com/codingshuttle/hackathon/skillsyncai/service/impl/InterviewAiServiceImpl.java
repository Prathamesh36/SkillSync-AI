package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.dto.EvaluationDTO;
import com.codingshuttle.hackathon.skillsyncai.service.InterviewAiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewAiServiceImpl implements InterviewAiService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are a professional technical interviewer conducting a mock interview.
            Rules:
            - Ask clear, concise questions
            - One question at a time
            - Adjust difficulty based on candidate's experience level
            """;

    private static final String QUESTION_GENERATION_PROMPT = """
            Generate ONE practical technical interview question for a candidate with the following profile:
            %s
            Question number: %d of 5
            Previous performance: %s
            Requirements:
            - Relevant to skills
            - Match experience level (%d years)
            Respond with ONLY the question text.
            """;

    private static final String EVALUATION_PROMPT = """
            Evaluate this interview answer:
            Question: %s
            Candidate Answer: %s
            Respond ONLY with a JSON object:
            {
              "score": <number 0-10>,
              "strengths": ["string"],
              "weaknesses": ["string"]
            }
            """;

    @Override
    public String generateFirstQuestion(String resumeSummary, int experienceYears) {
        String prompt = String.format(QUESTION_GENERATION_PROMPT, resumeSummary, 1, "Initial question",
                experienceYears);
        return chatClientBuilder.build().prompt().system(SYSTEM_PROMPT).user(prompt).call().content();
    }

    @Override
    public String generateNextQuestion(String resumeSummary, int experienceYears, int questionNumber,
            String performanceSummary) {
        String prompt = String.format(QUESTION_GENERATION_PROMPT, resumeSummary, questionNumber, performanceSummary,
                experienceYears);
        return chatClientBuilder.build().prompt().system(SYSTEM_PROMPT).user(prompt).call().content();
    }

    @Override
    public EvaluationDTO evaluateAnswer(String question, String answer, String resumeSummary) {
        String prompt = String.format(EVALUATION_PROMPT, question, answer);
        String response = chatClientBuilder.build().prompt().system(SYSTEM_PROMPT).user(prompt).call().content();
        return parseEvaluationResponse(response);
    }

    @Override
    public String generateFinalFeedback(String resumeSummary, List<Integer> scores) {
        double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        String prompt = String.format(
                "Generate final feedback for candidate: %s. Scores: %s, Average: %.1f. 2-3 sentences.", resumeSummary,
                scores.toString(), avgScore);
        return chatClientBuilder.build().prompt().system(SYSTEM_PROMPT).user(prompt).call().content();
    }

    @Override
    public String generateFirstQuestionForTopic(List<String> topics, String difficulty) {
        String prompt = String.format("Ask a %s question about: %s.", difficulty, String.join(", ", topics));
        return chatClientBuilder.build().prompt().system(SYSTEM_PROMPT).user(prompt).call().content();
    }

    @Override
    public String generateNextQuestionForTopic(List<String> topics, String difficulty, int questionNumber,
            String performanceSummary) {
        String prompt = String.format("Ask question %d (%s) about: %s. Previous performance: %s", questionNumber,
                difficulty, String.join(", ", topics), performanceSummary);
        return chatClientBuilder.build().prompt().system(SYSTEM_PROMPT).user(prompt).call().content();
    }

    @Override
    public String buildResumeSummary(String candidateName, List<String> skills, int experienceYears, String headline) {
        return String.format("Name: %s, Experience: %d, Role: %s, Skills: %s", candidateName, experienceYears, headline,
                String.join(", ", skills));
    }

    private EvaluationDTO parseEvaluationResponse(String response) {
        try {
            String cleaned = response.replaceAll("```json|```", "").trim();
            JsonNode root = objectMapper.readTree(cleaned);
            int score = root.has("score") ? root.get("score").asInt() : 5;
            List<String> sList = new ArrayList<>();
            List<String> wList = new ArrayList<>();
            if (root.has("strengths"))
                root.get("strengths").forEach(n -> sList.add(n.asText()));
            if (root.has("weaknesses"))
                root.get("weaknesses").forEach(n -> wList.add(n.asText()));
            return new EvaluationDTO(score, sList, wList, "Detailed feedback available above.");
        } catch (Exception e) {
            return new EvaluationDTO(5, List.of("Answer received"), List.of("Parsing error"),
                    "Error parsing AI response.");
        }
    }
}
