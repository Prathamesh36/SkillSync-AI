package com.codingshuttle.hackathon.skillsyncai.service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface VectorSearchService {
    List<Document> findSimilarResumes(String jobDescription, int topK);

    List<Document> findSimilarJobs(String query, int topK);
}
