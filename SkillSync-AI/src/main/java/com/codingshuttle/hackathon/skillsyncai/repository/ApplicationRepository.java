package com.codingshuttle.hackathon.skillsyncai.repository;

import com.codingshuttle.hackathon.skillsyncai.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Check if a candidate has already applied for a specific job.
     */
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    boolean existsByResumeId(Long resumeId);

    /**
     * Find all applications by a specific candidate.
     */
    List<Application> findByCandidateId(Long candidateId);

    /**
     * Find all applications for a specific job.
     */
    List<Application> findByJobId(Long jobId);

    /**
     * Find applications for a job and list of candidates.
     */
    List<Application> findByJobIdAndCandidateIdIn(Long jobId, List<Long> candidateIds);

    /**
     * Find all applications for jobs posted by a specific recruiter.
     */
    List<Application> findByJobPostedByIdOrderByAppliedAtDesc(Long recruiterId);

    long countByJobPostedById(Long recruiterId);
}
