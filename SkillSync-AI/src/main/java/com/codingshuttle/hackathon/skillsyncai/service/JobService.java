package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

        private final JobRepository jobRepository;
        private final VectorStore vectorStore;

        @Transactional
        public Job createJob(Job job) {
                // 1. Save to DB
                Job savedJob = jobRepository.save(job);

                // 2. Generate Embedding & Save to Vector Store
                saveJobToVectorStore(savedJob);

                return savedJob;
        }

        public Job getJob(Long id) {
                return jobRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        }

        public List<Job> getAllJobs() {
                return jobRepository.findAll();
        }

        public List<Job> getJobsByRecruiter(com.codingshuttle.hackathon.skillsyncai.entity.User recruiter) {
                return jobRepository.findByPostedBy(recruiter);
        }

        @Transactional
        public Job updateJob(Long id, Job updatedDetails) {
                Job existingJob = getJob(id);

                // Update fields
                // Note: Assuming updatedDetails contains the new values.
                // In a real scenario, we might use a mapper or explicit setters.
                // But here, since we are passed the entity, we'll assume the caller handled
                // mapping
                // OR we map relevant fields here.
                // Ideally, JobMapper should be used in Controller to update existingJob.
                // Let's assume existingJob is ALREADY updated by the caller (or we're passed
                // the entity to save).
                // Wait, correct pattern is service ref receives entity with updates or DTO.
                // Let's stick to: we save the passed entity.
                // Actually, for cleaner service logic, let's assume the passed `updatedDetails`
                // holds the new state and we merge it or we just save it if it's the same
                // object attached to persistence context.
                // To be safe and explicit:

                // 1. Update DB
                Job savedJob = jobRepository.save(updatedDetails);

                // 2. Update Vector Store
                // First delete existing embedding
                deleteJobFromVectorStore(id);

                // Then add new one
                saveJobToVectorStore(savedJob);

                return savedJob;
        }

        @Transactional
        public void deleteJob(Long id) {
                if (!jobRepository.existsById(id)) {
                        throw new ResourceNotFoundException("Job not found with id: " + id);
                }

                // 1. Delete from DB
                jobRepository.deleteById(id);

                // 2. Delete from Vector Store
                deleteJobFromVectorStore(id);
        }

        public List<Job> searchJobs(String query) {
                // Semantic search using Vector Store
                List<Document> similarDocuments = vectorStore.similaritySearch(
                                SearchRequest.builder()
                                                .query(query)
                                                .topK(5)
                                                .filterExpression(new FilterExpressionBuilder().eq("docType", "JOB")
                                                                .build())
                                                .build());

                // Extract Job IDs and fetch from DB
                List<Long> jobIds = similarDocuments.stream()
                                .map(doc -> Long.valueOf(doc.getMetadata().get("jobId").toString()))
                                .collect(Collectors.toList());

                return jobRepository.findAllById(jobIds);
        }

        private void saveJobToVectorStore(Job job) {
                String jobContent = "Job Title: " + job.getTitle() +
                                "\nDescription: " + job.getDescription() +
                                "\nSkills: " + String.join(", ", job.getSkillsRequired()) +
                                "\nLocation: " + job.getLocation();

                // Use deterministic UUID for easy deletion
                String vectorDocId = java.util.UUID.nameUUIDFromBytes(("job_" + job.getId()).getBytes()).toString();

                Document document = new Document(vectorDocId, jobContent, Map.of(
                                "jobId", job.getId(),
                                "jobType", job.getJobType().name(),
                                "docType", "JOB"));

                vectorStore.add(List.of(document));
                log.info("Saved job embedding with docId: {}", vectorDocId);
        }

        private void deleteJobFromVectorStore(Long jobId) {
                String vectorDocId = java.util.UUID.nameUUIDFromBytes(("job_" + jobId).getBytes()).toString();
                try {
                        vectorStore.delete(List.of(vectorDocId));
                        log.info("Deleted job embedding with docId: {}", vectorDocId);
                } catch (Exception e) {
                        log.warn("Failed to delete job embedding for jobId: {} with docId: {}", jobId, vectorDocId, e);
                }
        }
}
