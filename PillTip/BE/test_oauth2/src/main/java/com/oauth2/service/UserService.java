// PillTip\BE\src\main\java\com\example\oauth2\service\UserService.java
// author : mireutale
// date : 2025-05-19
// description : 사용자 서비스

package com.oauth2.service;

import com.oauth2.dto.LoginRequest;
import com.oauth2.dto.LoginResponse;
import com.oauth2.entity.LoginType;
import com.oauth2.entity.User;
import com.oauth2.entity.UserToken;
import com.oauth2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    // ID/PW 로그인
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getLoginType() != LoginType.IDPW) {
            throw new RuntimeException("This account is not an ID/PW account");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        UserToken userToken = tokenService.generateTokens(user.getId());
        
        return LoginResponse.builder()
                .accessToken(userToken.getAccessToken())
                .refreshToken(userToken.getRefreshToken())
                .build();
    }

    // 소셜 로그인
    public LoginResponse socialLogin(String socialId, LoginType loginType) {
        User user = userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getLoginType() != loginType) {
            throw new RuntimeException("Invalid login type");
        }

        UserToken userToken = tokenService.generateTokens(user.getId());
        
        return LoginResponse.builder()
                .accessToken(userToken.getAccessToken())
                .refreshToken(userToken.getRefreshToken())
                .build();
    }

    // 토큰 갱신
    public LoginResponse refreshToken(String refreshToken) {
        UserToken userToken = tokenService.refreshTokens(refreshToken);
        User user = userRepository.findById(userToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return LoginResponse.builder()
                .accessToken(userToken.getAccessToken())
                .refreshToken(userToken.getRefreshToken())
                .build();
    }

    // 현재 로그인한 사용자 정보 조회
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 이용약관 동의
    public User agreeToTerms(User user) {
        user.setTerms(true);
        return userRepository.save(user);
    }

    // 닉네임 업데이트
    public User updateNickname(User user, String nickname) {
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new RuntimeException("Nickname already exists");
        }
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    // 프로필 사진 업데이트
    public User updateProfilePhoto(User user, String photoUrl) {
        user.setProfilePhoto(photoUrl);
        return userRepository.save(user);
    }
}