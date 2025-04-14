package com.studybuddyai.service.impl;

import com.studybuddyai.dto.UserDto;
import com.studybuddyai.exception.UserNotFoundException;
import com.studybuddyai.mapper.UserMapper;
import com.studybuddyai.model.OAuth2UserInfo;
import com.studybuddyai.model.User;
import com.studybuddyai.model.enums.AuthProvider;
import com.studybuddyai.model.enums.Role;
import com.studybuddyai.repository.UserRepository;
import com.studybuddyai.service.ClientService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientServiceImpl implements ClientService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public ClientServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto getCurrentUser(OAuth2User principal) {
        if (principal == null) {
            throw new OAuth2AuthenticationException("Not authorized");
        }

        String email = principal.getAttribute("email");
        String providerId = principal.getName();

        AuthProvider provider = AuthProvider.GOOGLE;

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        }

        User user = existingUser.orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(extractUsername(principal));
            newUser.setProvider(provider);
            newUser.setProviderId(providerId);
            newUser.setRole(Role.USER);

            OAuth2UserInfo userInfo = new OAuth2UserInfo();
            userInfo.setUser(newUser);
            userInfo.setProvider(provider.toString());
            userInfo.setProviderId(providerId);
            userInfo.setEmail(email);
            userInfo.setName(principal.getAttribute("name"));
            userInfo.setImageUrl(principal.getAttribute("picture"));

            newUser.setOAuth2UserInfo(userInfo);

            return userRepository.save(newUser);
        });

        return userMapper.userToUserDto(user);
    }

    private String extractUsername(OAuth2User principal) {
        String email = principal.getAttribute("email");
        return email != null ? email.split("@")[0] : "user" + System.currentTimeMillis();
    }

    public User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth.getPrincipal() instanceof OAuth2User oauthUser)) {
            throw new RuntimeException("Invalid principal type");
        }

        UserDto currentUser = getCurrentUser(oauthUser);
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }

        return userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found!"));
    }
}
