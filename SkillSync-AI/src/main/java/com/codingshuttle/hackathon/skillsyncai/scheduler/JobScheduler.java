package com.codingshuttle.hackathon.skillsyncai.scheduler;

import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobScheduler {

    private final JobRepository jobRepository;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void closeExpiredJobs() {
        log.info("Running scheduled task to close expired jobs...");
        List<Job> expiredJobs = jobRepository.findExpiredActiveJobs();

        if (expiredJobs.isEmpty()) {
            log.info("No expired jobs found.");
            return;
        }

        log.info("Found {} expired jobs. Closing them now.", expiredJobs.size());

        for (Job job : expiredJobs) {
            job.setActive(false);
        }

        jobRepository.saveAll(expiredJobs);
        log.info("Successfully closed {} expired jobs.", expiredJobs.size());
    }
}
