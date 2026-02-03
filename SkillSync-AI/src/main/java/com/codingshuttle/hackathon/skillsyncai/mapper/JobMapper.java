package com.codingshuttle.hackathon.skillsyncai.mapper;

import com.codingshuttle.hackathon.skillsyncai.dto.JobCreateDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.JobResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobMapper {

    private final ModelMapper modelMapper;

    public Job toEntity(JobCreateDTO dto) {
        return modelMapper.map(dto, Job.class);
    }

    public JobResponseDTO toDTO(Job job) {
        JobResponseDTO dto = new JobResponseDTO(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getCompanyName(),
                job.getLocation(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getCurrency(),
                job.getJobType(),
                job.getEmploymentType(),
                job.getRequiredExperienceYears(),
                job.getSkillsRequired(),
                job.getPostedBy() != null ? job.getPostedBy().getId() : null,
                job.isActive(),
                job.getCreatedAt());
        return dto;
    }
}
