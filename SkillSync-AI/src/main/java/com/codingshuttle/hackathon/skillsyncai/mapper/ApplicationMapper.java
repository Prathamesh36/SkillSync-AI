package com.codingshuttle.hackathon.skillsyncai.mapper;

import com.codingshuttle.hackathon.skillsyncai.dto.ApplicationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Application;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationMapper {

    private final ModelMapper modelMapper;

    public ApplicationResponseDTO toDTO(Application application) {
        return new ApplicationResponseDTO(
                application.getId(),
                application.getUser().getId(),
                application.getUser().getName(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getStatus(),
                application.getMatchScore(),
                application.getAiAnalysis(),
                application.getAppliedAt());
    }
}
