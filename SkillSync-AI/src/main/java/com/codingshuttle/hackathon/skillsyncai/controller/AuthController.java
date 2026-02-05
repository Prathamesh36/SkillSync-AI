package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.AuthResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.LoginRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginDto) {
        log.info("Login attempt for email: {}", loginDto.email());
        return ResponseEntity.ok(authService.login(loginDto));
    }
}
