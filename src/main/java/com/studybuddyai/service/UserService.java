package com.studybuddyai.service;

import com.studybuddyai.dto.UserDto;
import com.studybuddyai.dto.UserRegistrationDto;
import com.studybuddyai.model.User;

public interface UserService {

    UserDto registerNewUserAccount(UserRegistrationDto user);

    UserDto findByUsername(String username);

    UserDto findByEmail(String email);

    UserDto findById(Long id);

    UserDto updateUser(Long id, UserRegistrationDto user);

    void deleteUser(Long id);

}
