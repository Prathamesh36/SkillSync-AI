package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

        private final JobRepository jobRepository;
        private final VectorStore vectorStore;

        public Job createJob(Job job) {
                // 1. Save to DB
                Job savedJob = jobRepository.save(job);

                // 2. Generate Embedding & Save to Vector Store
                String jobContent = "Job Title: " + job.getTitle() +
                                "\nDescription: " + job.getDescription() +
                                "\nSkills: " + String.join(", ", job.getSkillsRequired()) +
                                "\nLocation: " + job.getLocation();

                Document document = new Document(jobContent, Map.of(
                                "jobId", savedJob.getId(),
                                "jobType", job.getJobType().name()));

                vectorStore.add(List.of(document));

                return savedJob;
        }

        public List<Job> searchJobs(String query) {
                // Semantic search using Vector Store
                List<Document> similarDocuments = vectorStore.similaritySearch(
                                SearchRequest.builder().query(query).topK(5).build());

                // Extract Job IDs and fetch from DB
                List<Long> jobIds = similarDocuments.stream()
                                .map(doc -> Long.valueOf(doc.getMetadata().get("jobId").toString()))
                                .collect(Collectors.toList());

                return jobRepository.findAllById(jobIds);
        }
}
