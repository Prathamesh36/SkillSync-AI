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

    /**
     * Find all applications by a specific candidate.
     */
    List<Application> findByCandidateId(Long candidateId);

    /**
     * Find all applications for a specific job.
     */
    List<Application> findByJobId(Long jobId);
}
