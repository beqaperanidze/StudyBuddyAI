package com.studybuddyai.repository;

import com.studybuddyai.model.OAuth2UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuth2UserInfoRepository extends JpaRepository<OAuth2UserInfo, Long> {
    Optional<OAuth2UserInfo> findByUserId(Long userId);
    Optional<OAuth2UserInfo> findByProviderAndProviderId(String provider, String providerId);
}