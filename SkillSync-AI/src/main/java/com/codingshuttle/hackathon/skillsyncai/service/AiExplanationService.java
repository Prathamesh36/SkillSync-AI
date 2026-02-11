package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;

public interface AiExplanationService {
    String generateExplanation(Job job, Candidate candidate);
}
