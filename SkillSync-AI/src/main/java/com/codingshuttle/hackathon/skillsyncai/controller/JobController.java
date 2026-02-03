package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.JobCreateDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.JobResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.mapper.JobMapper;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import com.codingshuttle.hackathon.skillsyncai.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    @PostMapping
    public ResponseEntity<JobResponseDTO> createJob(
            @RequestHeader("X-User-Id") Long recruiterId,
            @Valid @RequestBody JobCreateDTO dto) {

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new RuntimeException("Recruiter not found"));

        Job job = jobMapper.toEntity(dto);
        job.setPostedBy(recruiter);

        Job created = jobService.createJob(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobMapper.toDTO(created));
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDTO>> searchJobs(@RequestParam String query) {
        List<Job> jobs = jobService.searchJobs(query);
        return ResponseEntity.ok(jobs.stream().map(jobMapper::toDTO).collect(Collectors.toList()));
    }
}
