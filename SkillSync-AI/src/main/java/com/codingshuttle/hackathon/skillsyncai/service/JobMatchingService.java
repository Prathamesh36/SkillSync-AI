package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.MatchedCandidateDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.entity.MatchResult;
import com.codingshuttle.hackathon.skillsyncai.entity.Resume;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.CandidateRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.JobRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.MatchResultRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.ResumeRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.ApplicationRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.JobInvitationRepository;
import com.codingshuttle.hackathon.skillsyncai.entity.Application;
import com.codingshuttle.hackathon.skillsyncai.entity.JobInvitation;
import com.codingshuttle.hackathon.skillsyncai.enums.JobInvitationStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMatchingService {

    private final JobRepository jobRepository;
    private final VectorSearchService vectorSearchService;
    private final ResumeRepository resumeRepository;
    private final CandidateRepository candidateRepository;
    private final AiExplanationService aiExplanationService;
    private final MatchResultRepository matchResultRepository;
    private final ApplicationRepository applicationRepository;
    private final JobInvitationRepository jobInvitationRepository;

    @Transactional
    public List<MatchedCandidateDTO> getMatchedCandidates(Long jobId, int topK) {
        log.info("Fetching matched candidates for jobId: {}", jobId);

        // 1. Fetch Job
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // 2. Vector Search using Job Description
        // Combining relevant fields for better semantic search
        String query = job.getTitle() + " " + job.getDescription() + " " + String.join(" ", job.getSkillsRequired());
        List<Document> similarDocs = vectorSearchService.findSimilarResumes(query, topK * 2); // Fetch more to allow for
                                                                                              // filtering

        if (similarDocs.isEmpty()) {
            return Collections.emptyList();
        }

        // Map ResumeId -> Vector Score (similarity)
        Map<Long, Double> vectorScores = new HashMap<>();
        for (Document doc : similarDocs) {
            if (doc.getMetadata().containsKey("resumeId")) {
                Long resumeId = Long.valueOf(doc.getMetadata().get("resumeId").toString());

                Double distance = 0.0;
                if (doc.getMetadata().containsKey("distance")) {
                    Object distObj = doc.getMetadata().get("distance");
                    distance = distObj instanceof Double ? (Double) distObj : Double.parseDouble(distObj.toString());
                } else if (doc.getMetadata().containsKey("score")) {
                    Object scoreObj = doc.getMetadata().get("score");
                    vectorScores.put(resumeId,
                            scoreObj instanceof Double ? (Double) scoreObj : Double.parseDouble(scoreObj.toString()));
                    continue;
                }

                // Similarity = 1 - distance (approx for cosine)
                vectorScores.put(resumeId, 1.0 - distance);
            }
        }

        List<Long> resumeIds = new ArrayList<>(vectorScores.keySet());

        // 3. Fetch Resumes -> Users -> Candidates
        List<Resume> resumes = resumeRepository.findAllById(resumeIds);

        // Optimize lookup by User ID
        Map<Long, Resume> resumeByUserId = resumes.stream()
                .collect(Collectors.toMap(r -> r.getUser().getId(), Function.identity()));

        Set<Long> userIds = resumeByUserId.keySet();
        List<Candidate> candidates = candidateRepository.findByUser_IdIn(userIds);

        List<MatchedCandidateDTO> results = new ArrayList<>();

        for (Candidate candidate : candidates) {
            // Find corresponding resume using efficient map lookup
            Resume resume = resumeByUserId.get(candidate.getUser().getId());

            if (resume == null || !vectorScores.containsKey(resume.getId()))
                continue;

            // 4. Hard Filters
            if (job.getRequiredExperienceYears() != null && candidate.getExperienceYears() != null) {
                if (candidate.getExperienceYears() < job.getRequiredExperienceYears()) {
                    continue; // Skip if experience not enough
                }
            }

            // 5. Scoring
            double vectorScore = vectorScores.get(resume.getId());
            double skillScore = calculateSkillScore(job.getSkillsRequired(), candidate.getSkills());
            double expScore = calculateExperienceScore(job.getRequiredExperienceYears(),
                    candidate.getExperienceYears());

            double finalScore = (0.6 * vectorScore) + (0.3 * skillScore) + (0.1 * expScore);

            MatchedCandidateDTO dto = MatchedCandidateDTO.builder()
                    .candidateId(candidate.getId())
                    .resumeId(resume.getId())
                    .name(candidate.getUser().getName())
                    .email(candidate.getUser().getEmail())
                    .experienceYears(candidate.getExperienceYears())
                    .skills(candidate.getSkills())
                    .location(candidate.getLocation())
                    .matchScore(finalScore)
                    .build();

            results.add(dto);
        }

        // Sort by final score descending
        results.sort(Comparator.comparingDouble(MatchedCandidateDTO::getMatchScore).reversed());

        // Limit to topK
        List<MatchedCandidateDTO> topResults = results.stream().limit(topK).collect(Collectors.toList());

        // 6. Explanation & Persistence (Top 5 only) & 7. Populate Invitation Status
        List<Long> candidateIds = topResults.stream().map(MatchedCandidateDTO::getCandidateId).toList();

        List<Application> applications = applicationRepository.findByJobIdAndCandidateIdIn(jobId, candidateIds);
        Set<Long> appliedCandidateIds = applications.stream().map(a -> a.getCandidate().getId())
                .collect(Collectors.toSet());

        List<JobInvitation> invitations = jobInvitationRepository.findByJobIdAndCandidateIdIn(jobId, candidateIds);
        // Map candidateId -> latest meaningful status
        Map<Long, JobInvitationStatus> invitationStatusMap = new HashMap<>();
        for (JobInvitation inv : invitations) {
            // Priority: SENT > others. If duplicate, keep SENT.
            // Simplified logic: just store the status. If multiple, last one wins
            // (refinement needed if many invites)
            // But usually unique per job-candidate for SENT.
            invitationStatusMap.put(inv.getCandidate().getId(), inv.getStatus());
        }

        for (int i = 0; i < topResults.size(); i++) {
            MatchedCandidateDTO dto = topResults.get(i);

            // Set invitation status
            if (appliedCandidateIds.contains(dto.getCandidateId())) {
                dto.setInvitationStatus("APPLIED");
            } else if (invitationStatusMap.containsKey(dto.getCandidateId())) {
                dto.setInvitationStatus(invitationStatusMap.get(dto.getCandidateId()).name());
            }

            // Only generate explanation for top 5
            if (i < 5) {
                Candidate candidate = candidateRepository.findById(dto.getCandidateId()).orElse(null);

                if (candidate != null) {
                    // Check if we have a recent match result to reuse explanation
                    Optional<MatchResult> existingMatch = matchResultRepository.findByJobIdAndCandidateId(jobId,
                            candidate.getId());

                    String explanation;
                    if (existingMatch.isPresent() && existingMatch.get().getExplanation() != null) {
                        explanation = existingMatch.get().getExplanation();
                    } else {
                        explanation = aiExplanationService.generateExplanation(job, candidate);
                    }

                    dto.setExplanation(explanation);

                    // Persist
                    MatchResult matchResult = existingMatch.orElse(MatchResult.builder()
                            .jobId(jobId)
                            .candidateId(candidate.getId())
                            .build());
                    matchResult.setMatchScore(dto.getMatchScore());
                    matchResult.setExplanation(explanation);
                    matchResultStore(matchResult);
                }
            }
        }

        return topResults;
    }

    private void matchResultStore(MatchResult matchResult) {
        matchResultRepository.save(matchResult);
    }

    private double calculateSkillScore(List<String> jobSkills, List<String> candidateSkills) {
        if (jobSkills == null || jobSkills.isEmpty())
            return 1.0;
        if (candidateSkills == null || candidateSkills.isEmpty())
            return 0.0;

        Set<String> jobSkillSet = jobSkills.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> candidateSkillSet = candidateSkills.stream().map(String::toLowerCase).collect(Collectors.toSet());

        long matchCount = jobSkillSet.stream().filter(candidateSkillSet::contains).count();
        return (double) matchCount / jobSkillSet.size();
    }

    private double calculateExperienceScore(Integer jobExp, Integer candidateExp) {
        if (jobExp == null || jobExp == 0)
            return 1.0;
        if (candidateExp == null)
            return 0.0;

        if (candidateExp >= jobExp)
            return 1.0;
        return (double) candidateExp / jobExp;
    }
}
