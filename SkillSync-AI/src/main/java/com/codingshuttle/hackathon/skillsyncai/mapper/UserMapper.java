package com.codingshuttle.hackathon.skillsyncai.mapper;

import com.codingshuttle.hackathon.skillsyncai.dto.UserCreateDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.UserResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.UserUpdateDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ModelMapper modelMapper;

    public User toEntity(UserCreateDTO dto) {
        return modelMapper.map(dto, User.class);
    }

    public void updateEntity(User user, UserUpdateDTO dto) {
        if (dto.name() != null)
            user.setName(dto.name());
        if (dto.bio() != null)
            user.setBio(dto.bio());
        if (dto.linkedInUrl() != null)
            user.setLinkedInUrl(dto.linkedInUrl());
        if (dto.portfolioUrl() != null)
            user.setPortfolioUrl(dto.portfolioUrl());
        if (dto.skills() != null)
            user.setSkills(dto.skills());
        if (dto.experienceYears() != null)
            user.setExperienceYears(dto.experienceYears());
    }

    public UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getBio(),
                user.getLinkedInUrl(),
                user.getPortfolioUrl(),
                user.getSkills(),
                user.getExperienceYears(),
                user.getCreatedAt());
    }
}
