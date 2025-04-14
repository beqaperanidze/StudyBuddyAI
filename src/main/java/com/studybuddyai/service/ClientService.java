package com.studybuddyai.service;

import com.studybuddyai.dto.UserDto;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public interface ClientService {
    UserDto getCurrentUser(OAuth2User principal);
}
