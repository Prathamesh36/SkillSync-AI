package com.codingshuttle.hackathon.skillsyncai.service;

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
public class VectorSearchService {

    private final VectorStore vectorStore;

    /**
     * Searches for resume IDs that match the given job description.
     * Uses metadata filter to ensure only resumes are returned.
     *
     * @param jobDescription The text description of the job to match against.
     * @param topK           The number of top results to retrieve.
     * @return List of Document objects containing resume metadata and similarity
     *         scores.
     */
    public List<Document> findSimilarResumes(String jobDescription, int topK) {
        log.info("Searching for similar resumes with topK: {}", topK);

        // Filter to ensure we only get documents that have docType = "RESUME"
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

    /**
     * Searches for Job IDs that match the given resume content or query.
     * Uses metadata filter to ensure only Jobs are returned.
     *
     * @param query The text to match against (e.g., resume content).
     * @param topK  The number of top results to retrieve.
     * @return List of Document objects containing job metadata and similarity
     *         scores.
     */
    public List<Document> findSimilarJobs(String query, int topK) {
        log.info("Searching for similar jobs with topK: {}", topK);

        FilterExpressionBuilder b = new FilterExpressionBuilder();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression(b.eq("docType", "JOB").build())
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.info("Found {} similar jobs", results.size());
        return results;
    }
}
