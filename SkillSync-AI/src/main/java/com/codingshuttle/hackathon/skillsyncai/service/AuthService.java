package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.AuthResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.LoginRequestDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO loginRequestInfo);
}
