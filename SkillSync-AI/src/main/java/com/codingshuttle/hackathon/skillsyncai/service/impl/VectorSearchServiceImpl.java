package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.service.VectorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorSearchServiceImpl implements VectorSearchService {

    private final VectorStore vectorStore;

    @Override
    public List<Document> findSimilarResumes(String jobDescription, int topK) {
        log.info("Searching for similar resumes with topK: {}", topK);

        FilterExpressionBuilder b = new FilterExpressionBuilder();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(jobDescription)
                .topK(topK)
                .filterExpression(b.eq("docType", "RESUME").build())
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.info("Found {} similar documents", results.size());
        return results;
    }

    @Override
    public List<Document> findSimilarJobs(String query, int topK) {
        log.info("Searching for similar jobs with topK: {}", topK);

        FilterExpressionBuilder b = new FilterExpressionBuilder();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.3)
                .filterExpression(b.eq("docType", "JOB").build())
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.info("Found {} similar jobs", results.size());
        return results;
    }
}
