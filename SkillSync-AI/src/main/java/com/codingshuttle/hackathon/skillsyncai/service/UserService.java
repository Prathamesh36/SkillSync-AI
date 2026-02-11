package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.UserCreateDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.UserResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserCreateDTO dto);

    UserResponseDTO getUserById(Long id);

    UserResponseDTO getUserByEmail(String email);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO updateUser(Long id, UserUpdateDTO dto);

    void deleteUser(Long id);
}
