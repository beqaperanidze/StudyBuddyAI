package com.studybuddyai.security.oauth2;

import com.studybuddyai.exception.OAuth2AuthenticationProcessingException;
import com.studybuddyai.model.enums.AuthProvider;
import com.studybuddyai.model.User;
import com.studybuddyai.model.enums.Role;
import com.studybuddyai.repository.OAuth2UserInfoRepository;
import com.studybuddyai.repository.UserRepository;
import com.studybuddyai.security.UserPrincipal;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    private final OAuth2UserInfoRepository oAuth2UserInfoRepository;

    public CustomOAuth2UserService(UserRepository userRepository, OAuth2UserInfoRepository oAuth2UserInfoRepository) {
        this.userRepository = userRepository;
        this.oAuth2UserInfoRepository = oAuth2UserInfoRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) throws OAuth2AuthenticationProcessingException {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))) {
                throw new OAuth2AuthenticationProcessingException("You've signed up with " +
                        user.getProvider() + ". Please use your " + user.getProvider() + " account to login");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();

        user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()));
        user.setProviderId(oAuth2UserInfo.getProviderId());
        user.setUsername(oAuth2UserInfo.getName().replace(" ", ""));
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setPassword(null);
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        com.studybuddyai.model.OAuth2UserInfo userInfoEntity = new com.studybuddyai.model.OAuth2UserInfo();
        userInfoEntity.setProvider(oAuth2UserRequest.getClientRegistration().getRegistrationId());
        userInfoEntity.setProviderId(oAuth2UserInfo.getProviderId());
        userInfoEntity.setName(oAuth2UserInfo.getName());
        userInfoEntity.setEmail(oAuth2UserInfo.getEmail());
        userInfoEntity.setImageUrl(oAuth2UserInfo.getImageUrl());
        userInfoEntity.setUser(savedUser);

        oAuth2UserInfoRepository.save(userInfoEntity);

        return savedUser;
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setUsername(oAuth2UserInfo.getName());

        com.studybuddyai.model.OAuth2UserInfo userInfoEntity = oAuth2UserInfoRepository.findByUserId(existingUser.getId())
                .orElse(new com.studybuddyai.model.OAuth2UserInfo());

        userInfoEntity.setName(oAuth2UserInfo.getName());
        userInfoEntity.setImageUrl(oAuth2UserInfo.getImageUrl());
        userInfoEntity.setUser(existingUser);

        oAuth2UserInfoRepository.save(userInfoEntity);

        return userRepository.save(existingUser);
    }
}