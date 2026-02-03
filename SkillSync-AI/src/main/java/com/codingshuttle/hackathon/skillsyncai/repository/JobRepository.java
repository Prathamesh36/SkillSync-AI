package com.codingshuttle.hackathon.skillsyncai.repository;

import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
}
