package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.RecommendedJobResponse;
import com.codingshuttle.hackathon.skillsyncai.entity.User;

import java.util.List;

public interface CandidateJobRecommendationService {
    List<RecommendedJobResponse> getRecommendations(User user, int topK, Double minScore, String locationFilter);

    String getExplanation(Long userId, Long jobId);
}
