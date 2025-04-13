package com.studybuddyai.service;

import com.studybuddyai.dto.UserDto;
import com.studybuddyai.dto.UserRegistrationDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    UserDto registerNewUser(UserRegistrationDto user);

    List<UserDto> findAll();

    UserDto findByUsername(String username);

    UserDto findByEmail(String email);

    UserDto findById(Long id);

    UserDto updateUser(Long id, UserRegistrationDto user);

    void deleteUser(Long id);

    void deleteAllUser();

}
