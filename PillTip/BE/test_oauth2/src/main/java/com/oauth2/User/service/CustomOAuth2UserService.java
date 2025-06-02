// author : mireutale
// description : 커스텀 OAuth2 사용자 서비스
package com.oauth2.User.service;

import com.oauth2.User.entity.LoginType;
import com.oauth2.User.entity.User;
import com.oauth2.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    // Spring Security에서 제공하는 DefaultOAuth2UserService를 상속받아 커스텀 OAuth2 사용자 서비스 구현
    // Spring Security에서 로그인을 처리한 뒤, 사용자 정보를 DB에 저장
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest); // 소셜 로그인으로부터 사용자 정보 로드
        saveOrUpdate(userRequest.getClientRegistration().getRegistrationId(), oauth2User);  // 사용자 저장 또는 업데이트
        return oauth2User;  // 최종 사용자 객체 반환
    }

    private User saveOrUpdate(String registrationId, OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        final String name;
        final String picture;
        final String socialId;

        if ("google".equals(registrationId)) {
            name = (String) attributes.get("name");
            picture = (String) attributes.get("picture");
            socialId = (String) attributes.get("sub");
        } else if ("kakao".equals(registrationId)) {
            socialId = String.valueOf(attributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account"); // 카카오 계정 정보
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile"); // 카카오 프로필 정보
            name = (String) profile.get("nickname"); 
            picture = (String) profile.get("profile_image_url");
        } else {
            throw new IllegalArgumentException("Unsupported registration ID: " + registrationId);
        }
    
        return userRepository.findBySocialId(socialId)
                .map(user -> user.update(name, picture))
                .orElseGet(() -> userRepository.save(User.builder() // 소셜 로그인 유저 정보 저장
                        .loginType(LoginType.SOCIAL)
                        .socialId(socialId)
                        .nickname(name)
                        .profilePhoto(picture)
                        .terms(false)
                        .build()));
    }
} 