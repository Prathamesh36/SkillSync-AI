package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.ParsedResumeDTO;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

public interface AIService {
    List<Double> generateEmbedding(String text);

    void storeResumeEmbedding(Long resumeId, String resumeContent, Map<String, Object> metadata);

    List<Document> searchSimilarResumes(String query, int topK);

    ParsedResumeDTO parseResume(Resource resumeFile);
}
