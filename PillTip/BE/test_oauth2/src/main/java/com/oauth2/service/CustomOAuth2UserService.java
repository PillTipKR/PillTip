// PillTip\BE\src\main\java\com\oauth2\service\CustomOAuth2UserService.java
// author : mireutale
// date : 2025-05-19
// description : 커스텀 OAuth2 사용자 서비스

package com.oauth2.service;

import com.oauth2.entity.LoginType;
import com.oauth2.entity.User;
import com.oauth2.repository.UserRepository;
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
        saveOrUpdate(oauth2User);  // 사용자 저장 또는 업데이트
        return oauth2User;  // 최종 사용자 객체 반환
    }

    private User saveOrUpdate(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String socialId = (String) attributes.get("sub");

        User user = userRepository.findBySocialId(socialId) // socialId(token)으로 유저 찾기
                .map(entity -> entity.update(name, picture)) // 이미 존재하는 경우, 유저 업데이트
                .orElse(User.builder() // 존재하지 않는 경우, 유저 생성
                        .loginType(LoginType.social) // 로그인 타입 소셜
                        .socialId(socialId) // 소셜 아이디
                        .nickname(name) // 닉네임
                        .profilePhotoUrl(picture) // 프로필 사진
                        .agreedTerms(false) // 이용약관 동의
                        .build());

        return userRepository.save(user); // 유저 저장
    }
} 