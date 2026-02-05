package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.AuthResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.LoginRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.UserResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.mapper.UserMapper;
import com.codingshuttle.hackathon.skillsyncai.security.JwtTokenProvider;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthResponseDTO login(LoginRequestDTO loginRequestInfo) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestInfo.email(),
                        loginRequestInfo.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequestInfo.email())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with email: " + loginRequestInfo.email()));

        return new AuthResponseDTO(token, userMapper.toDTO(user));
    }
}
