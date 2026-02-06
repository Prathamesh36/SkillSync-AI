package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.EvaluationDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Service specifically for mock interview functionality.
 * Handles all AI prompts for question generation, answer evaluation, and
 * feedback.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewAiService {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    // ================ PROMPT CONSTANTS ================

    private static final String SYSTEM_PROMPT = """
            You are a professional technical interviewer conducting a mock interview.
            Rules:
            - Ask clear, concise questions
            - One question at a time
            - Adjust difficulty based on candidate's experience level
            - Be encouraging but realistic
            - Focus on practical knowledge
            """;

    private static final String QUESTION_GENERATION_PROMPT = """
            Generate ONE practical technical interview question for a candidate with the following profile:

            %s

            Question number: %d of 5
            Previous performance: %s

            Requirements:
            - The question should be relevant to their skills
            - Difficulty should match their experience level (%d years)
            - If previous answers were weak, ask an easier question
            - If previous answers were strong, increase difficulty

            Respond with ONLY the question text, nothing else.
            """;

    private static final String EVALUATION_PROMPT = """
            Evaluate this interview answer:

            Question: %s
            Candidate Answer: %s
            Candidate Profile: %s

            Respond ONLY with a JSON object in this exact format:
            {
              "score": <number 0-10>,
              "strengths": ["strength 1", "strength 2"],
              "weaknesses": ["weakness 1", "weakness 2"]
            }
            """;

    private static final String FINAL_FEEDBACK_PROMPT = """
            Generate final interview feedback based on these evaluation scores:

            Candidate Profile: %s

            Individual Question Scores: %s
            Average Score: %.1f/10

            Provide a concise 2-3 sentence summary of:
            - Overall performance
            - Key strengths demonstrated
            - Areas for improvement

            Respond with ONLY the feedback text, nothing else.
            """;

    // ================ PUBLIC METHODS ================

    /**
     * Generate the first interview question based on resume summary.
     */
    public String generateFirstQuestion(String resumeSummary, int experienceYears) {
        log.info("Generating first interview question");

        String prompt = String.format(QUESTION_GENERATION_PROMPT,
                resumeSummary, 1, "First question - no previous answers", experienceYears);

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content();

        log.debug("Generated first question: {}", response);
        return response != null ? response.trim()
                : "Tell me about your experience with your primary programming language.";
    }

    /**
     * Generate the next interview question based on previous performance.
     */
    public String generateNextQuestion(String resumeSummary, int experienceYears, int questionNumber,
            String performanceSummary) {
        log.info("Generating question {} based on performance", questionNumber);

        String prompt = String.format(QUESTION_GENERATION_PROMPT,
                resumeSummary, questionNumber, performanceSummary, experienceYears);

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content();

        log.debug("Generated question {}: {}", questionNumber, response);
        return response != null ? response.trim()
                : "Can you explain a challenging technical problem you solved recently?";
    }

    /**
     * Evaluate a candidate's answer and return structured feedback.
     */
    public EvaluationDTO evaluateAnswer(String question, String answer, String resumeSummary) {
        log.info("Evaluating candidate answer");

        String prompt = String.format(EVALUATION_PROMPT, question, answer, resumeSummary);

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content();

        log.debug("Raw evaluation response: {}", response);
        return parseEvaluationResponse(response);
    }

    /**
     * Generate final interview feedback and summary.
     */
    public String generateFinalFeedback(String resumeSummary, List<Integer> scores) {
        log.info("Generating final interview feedback");

        double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        String scoresStr = scores.toString();

        String prompt = String.format(FINAL_FEEDBACK_PROMPT, resumeSummary, scoresStr, avgScore);

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(prompt)
                .call()
                .content();

        log.debug("Generated final feedback: {}", response);
        return response != null ? response.trim()
                : "Interview completed. Review your answers for areas of improvement.";
    }

    // ================ HELPER METHODS ================

    /**
     * Parse AI response into EvaluationDTO.
     * Handles malformed JSON gracefully.
     */
    private EvaluationDTO parseEvaluationResponse(String response) {
        try {
            // Clean response - remove markdown code blocks if present
            String cleanedResponse = response;
            if (response.contains("```json")) {
                cleanedResponse = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            } else if (response.contains("```")) {
                cleanedResponse = response.replaceAll("```\\s*", "");
            }

            JsonNode root = objectMapper.readTree(cleanedResponse.trim());

            int score = root.has("score") ? root.get("score").asInt() : 5;

            List<String> strengths = new ArrayList<>();
            if (root.has("strengths") && root.get("strengths").isArray()) {
                for (JsonNode s : root.get("strengths")) {
                    strengths.add(s.asText());
                }
            }

            List<String> weaknesses = new ArrayList<>();
            if (root.has("weaknesses") && root.get("weaknesses").isArray()) {
                for (JsonNode w : root.get("weaknesses")) {
                    weaknesses.add(w.asText());
                }
            }

            return new EvaluationDTO(score, strengths, weaknesses);

        } catch (Exception e) {
            log.warn("Failed to parse evaluation response: {}", response, e);
            return new EvaluationDTO(5, List.of("Answer provided"), List.of("Could not fully evaluate"));
        }
    }

    /**
     * Build a compact resume summary for AI prompts.
     */
    public String buildResumeSummary(String candidateName, List<String> skills, int experienceYears, String headline) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(candidateName != null ? candidateName : "Candidate").append("\n");
        sb.append("Experience: ").append(experienceYears).append(" years\n");
        sb.append("Role: ").append(headline != null ? headline : "Software Developer").append("\n");
        sb.append("Skills: ").append(skills != null ? String.join(", ", skills) : "General programming");
        return sb.toString();
    }
}
