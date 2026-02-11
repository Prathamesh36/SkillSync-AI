package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.dto.UserCreateDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.UserResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.UserUpdateDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import com.codingshuttle.hackathon.skillsyncai.entity.Recruiter;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.mapper.UserMapper;
import com.codingshuttle.hackathon.skillsyncai.repository.CandidateRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.RecruiterRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import com.codingshuttle.hackathon.skillsyncai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RecruiterRepository recruiterRepository;
    private final CandidateRepository candidateRepository;
    private final UserMapper userMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO dto) {
        log.info("Attempting to create user with email: {}", dto.email());
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            log.warn("User creation failed. Email already exists: {}", dto.email());
            throw new BadRequestException("Email already exists");
        }
        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        if (savedUser.getRole() == com.codingshuttle.hackathon.skillsyncai.enums.UserRole.RECRUITER) {
            Recruiter recruiter = new Recruiter();
            recruiter.setUser(savedUser);
            recruiter.setCompanyName(dto.companyName());
            recruiter.setDesignation(dto.designation());
            recruiter.setCompanyWebsite(dto.companyWebsite());
            recruiterRepository.save(recruiter);
            log.info("Recruiter profile created for user id: {}", savedUser.getId());
        } else if (savedUser.getRole() == com.codingshuttle.hackathon.skillsyncai.enums.UserRole.CANDIDATE) {
            Candidate candidate = new Candidate();
            candidate.setUser(savedUser);
            candidate.setLocation(dto.location() != null ? dto.location() : "");
            candidate.setExperienceYears(dto.experienceYears() != null ? dto.experienceYears() : 0);
            candidate.setSkills(dto.skills() != null ? dto.skills() : java.util.Collections.emptyList());
            candidate.setHeadline(dto.headline());
            candidateRepository.save(candidate);
            log.info("Candidate profile created for user id: {}", savedUser.getId());
        }

        return userMapper.toDTO(savedUser);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        return userMapper.toDTO(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        return userMapper.toDTO(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email)));
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateDTO dto) {
        log.info("Updating user profile for id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userMapper.updateEntity(user, dto);

        if (user.getRole() == com.codingshuttle.hackathon.skillsyncai.enums.UserRole.RECRUITER) {
            Recruiter recruiter = recruiterRepository.findByUserId(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found for user: " + id));
            userMapper.updateRecruiter(recruiter, dto);
            recruiterRepository.save(recruiter);
        } else if (user.getRole() == com.codingshuttle.hackathon.skillsyncai.enums.UserRole.CANDIDATE) {
            Candidate candidate = candidateRepository.findByUserId(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + id));
            if (dto.skills() != null)
                candidate.setSkills(dto.skills());
            if (dto.experienceYears() != null)
                candidate.setExperienceYears(dto.experienceYears());
            if (dto.headline() != null)
                candidate.setHeadline(dto.headline());
            if (dto.location() != null)
                candidate.setLocation(dto.location());
            candidateRepository.save(candidate);
        }

        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }
}
