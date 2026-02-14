package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.service.AiExplanationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiExplanationServiceImpl implements AiExplanationService {

    private final ChatClient.Builder chatClientBuilder;

    @Override
    public String generateExplanation(Job job, Candidate candidate) {
        try {
            String candidateSkills = candidate.getSkills() != null ? String.join(", ", candidate.getSkills()) : "N/A";
            String jobSkills = job.getSkillsRequired() != null ? String.join(", ", job.getSkillsRequired()) : "N/A";

            String promptText = String.format("""
                    You are an expert HR recruiter. Explain why this candidate is a good match for the job.

                    Job Title: %s
                    Job Skills Required: %s
                    Job Experience Required: %d-%d years

                    Candidate Experience: %d years
                    Candidate Skills: %s
                    Candidate Headline: %s

                    Output Requirement:
                    - Provide 2-3 short bullet points explaining the match.
                    - Be concise and professional.
                    - Do not mention missing skills negatively, focus on the match.
                    """,
                    job.getTitle(),
                    jobSkills,
                    job.getMinExperienceYears() != null ? job.getMinExperienceYears() : 0,
                    job.getMaxExperienceYears() != null ? job.getMaxExperienceYears() : 99,
                    candidate.getExperienceYears() != null ? candidate.getExperienceYears() : 0,
                    candidateSkills,
                    candidate.getHeadline() != null ? candidate.getHeadline() : "N/A");

            String response = chatClientBuilder.build().prompt()
                    .user(promptText)
                    .call()
                    .content();

            return response != null ? response : "Explanation not available.";
        } catch (Exception e) {
            log.error("Error generating AI explanation for candidateId: {}", candidate.getId(), e);
            return "Could not generate explanation due to an internal error.";
        }
    }
}
