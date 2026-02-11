package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.MatchedCandidateDTO;

import java.util.List;

public interface JobMatchingService {
    List<MatchedCandidateDTO> getMatchedCandidates(Long jobId, int topK, String recruiterEmail);
}
