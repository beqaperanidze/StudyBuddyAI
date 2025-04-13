package com.studybuddyai.service.impl;

import com.studybuddyai.dto.UserDto;
import com.studybuddyai.dto.UserRegistrationDto;
import com.studybuddyai.exception.UserNotFoundException;
import com.studybuddyai.mapper.UserMapper;
import com.studybuddyai.model.User;
import com.studybuddyai.model.enums.Role;
import com.studybuddyai.repository.UserRepository;
import com.studybuddyai.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserDto registerNewUser(UserRegistrationDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newUser = userMapper.userRegistrationDtoToUser(userDto);
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        newUser.setRole(Role.USER);
        User savedUser = userRepository.save(newUser);
        return userMapper.userToUserDto(savedUser);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UserNotFoundException("User not found with username: " + username));
        return userMapper.userToUserDto(user);
    }

    @Override
    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException("User not found with email: " + email));
        return userMapper.userToUserDto(user);
    }

    @Override
    public UserDto findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("User not found with id: " + id));
        return userMapper.userToUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserRegistrationDto userDto) {
        User existingUser = userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("User not found with id: " + id));

        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.userToUserDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public void deleteAllUser() {
        userRepository.deleteAll();
    }
}
