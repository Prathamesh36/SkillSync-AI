package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.dto.MatchedCandidateDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.*;
import com.codingshuttle.hackathon.skillsyncai.enums.JobInvitationStatus;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.*;
import com.codingshuttle.hackathon.skillsyncai.service.AiExplanationService;
import com.codingshuttle.hackathon.skillsyncai.service.JobMatchingService;
import com.codingshuttle.hackathon.skillsyncai.service.VectorSearchService;
import com.codingshuttle.hackathon.skillsyncai.service.MatchScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobMatchingServiceImpl implements JobMatchingService {

    private final JobRepository jobRepository;
    private final VectorSearchService vectorSearchService;
    private final ResumeRepository resumeRepository;
    private final CandidateRepository candidateRepository;
    private final AiExplanationService aiExplanationService;
    private final MatchResultRepository matchResultRepository;
    private final ApplicationRepository applicationRepository;
    private final JobInvitationRepository jobInvitationRepository;
    private final MatchScoreCalculator matchScoreCalculator;

    @Override
    @Transactional
    public List<MatchedCandidateDTO> getMatchedCandidates(Long jobId, int topK, String recruiterEmail) {
        log.info("Fetching matched candidates for jobId: {} by recruiter: {}", jobId, recruiterEmail);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.getPostedBy().getEmail().equals(recruiterEmail)) {
            throw new AccessDeniedException("You are not authorized to view matches for this job.");
        }

        String query = job.getTitle() + " " + job.getDescription() + " " + String.join(" ", job.getSkillsRequired());

        List<Document> similarDocs;
        try {
            similarDocs = vectorSearchService.findSimilarResumes(query, topK * 2);
        } catch (Exception e) {
            log.error("Vector search failed for jobId: {} (likely AI rate limit).", jobId, e);
            return Collections.emptyList();
        }

        if (similarDocs.isEmpty()) {
            return Collections.emptyList();
        }

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
                vectorScores.put(resumeId, 1.0 - distance);
            }
        }

        List<Long> resumeIds = new ArrayList<>(vectorScores.keySet());
        List<Resume> resumes = resumeRepository.findAllById(resumeIds);
        Map<Long, Resume> resumeByUserId = resumes.stream()
                .collect(Collectors.toMap(r -> r.getUser().getId(), Function.identity()));
        List<Candidate> candidates = candidateRepository.findByUser_IdIn(resumeByUserId.keySet());

        List<MatchedCandidateDTO> results = new ArrayList<>();
        for (Candidate candidate : candidates) {
            Resume resume = resumeByUserId.get(candidate.getUser().getId());
            if (resume == null || !vectorScores.containsKey(resume.getId()))
                continue;

            if (job.getMinExperienceYears() != null && candidate.getExperienceYears() != null) {
                if (candidate.getExperienceYears() < job.getMinExperienceYears())
                    continue;
            }

            double skillOverlap = matchScoreCalculator.calculateSkillOverlap(job.getSkillsRequired(),
                    candidate.getSkills());
            if (skillOverlap < MatchScoreCalculator.MIN_SKILL_OVERLAP)
                continue;

            double finalScore = matchScoreCalculator.calculateScore(job.getSkillsRequired(), candidate.getSkills(),
                    job.getMinExperienceYears(), job.getMaxExperienceYears(), candidate.getExperienceYears());

            results.add(MatchedCandidateDTO.builder()
                    .candidateId(candidate.getId())
                    .resumeId(resume.getId())
                    .name(candidate.getUser().getName())
                    .email(candidate.getUser().getEmail())
                    .experienceYears(candidate.getExperienceYears())
                    .skills(candidate.getSkills())
                    .location(candidate.getLocation())
                    .matchScore(finalScore)
                    .build());
        }

        results.sort(Comparator.comparingDouble(MatchedCandidateDTO::getMatchScore).reversed());
        List<MatchedCandidateDTO> topResults = results.stream().limit(topK).collect(Collectors.toList());

        List<Long> candidateIds = topResults.stream().map(MatchedCandidateDTO::getCandidateId).toList();
        List<Application> applications = applicationRepository.findByJobIdAndCandidateIdIn(jobId, candidateIds);
        Set<Long> appliedCandidateIds = applications.stream().map(a -> a.getCandidate().getId())
                .collect(Collectors.toSet());

        List<JobInvitation> invitations = jobInvitationRepository.findByJobIdAndCandidateIdIn(jobId, candidateIds);
        Map<Long, JobInvitationStatus> invitationStatusMap = new HashMap<>();
        for (JobInvitation inv : invitations) {
            invitationStatusMap.put(inv.getCandidate().getId(), inv.getStatus());
        }

        for (int i = 0; i < topResults.size(); i++) {
            MatchedCandidateDTO dto = topResults.get(i);
            if (appliedCandidateIds.contains(dto.getCandidateId())) {
                dto.setInvitationStatus("APPLIED");
            } else if (invitationStatusMap.containsKey(dto.getCandidateId())) {
                dto.setInvitationStatus(invitationStatusMap.get(dto.getCandidateId()).name());
            }

            if (i < 5) {
                Candidate candidate = candidateRepository.findById(dto.getCandidateId()).orElse(null);
                if (candidate != null) {
                    Optional<MatchResult> existingMatch = matchResultRepository.findByJobIdAndCandidateId(jobId,
                            candidate.getId());
                    String explanation = (existingMatch.isPresent()
                            && existingMatch.get().getRecruiterExplanation() != null)
                                    ? existingMatch.get().getRecruiterExplanation()
                                    : aiExplanationService.generateExplanation(job, candidate);

                    dto.setExplanation(explanation);
                    MatchResult matchResult = existingMatch
                            .orElse(MatchResult.builder().jobId(jobId).candidateId(candidate.getId()).build());
                    matchResult.setMatchScore(dto.getMatchScore());
                    matchResult.setRecruiterExplanation(explanation);
                    matchResultRepository.save(matchResult);
                }
            }
        }
        return topResults;
    }
}
