package com.codingshuttle.hackathon.skillsyncai.repository;

import com.codingshuttle.hackathon.skillsyncai.entity.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    Optional<Recruiter> findByUserId(Long userId);
}
