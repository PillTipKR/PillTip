// PillTip\BE\src\main\java\com\example\oauth2\service\UserService.java
// author : mireutale
// date : 2025-05-19
// description : 사용자 서비스

package com.oauth2.service;

import com.oauth2.dto.LoginRequest;
import com.oauth2.dto.SignupRequest;
import com.oauth2.entity.Gender;
import com.oauth2.entity.LoginType;
import com.oauth2.entity.User;
import com.oauth2.entity.UserProfile;
import com.oauth2.entity.Interests;
import com.oauth2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service // 스프링 서비스 빈으로 등록
@RequiredArgsConstructor // 생성자 주입
@Transactional // 트랜잭션 관리
public class UserService {
    private final UserRepository userRepository; // 유저 저장소 접근 객체
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화

    // ID/PW 로그인
    public User login(LoginRequest request) {
        User user = userRepository.findByUuid(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getLoginType() != LoginType.idpw) {
            throw new RuntimeException("This account is not an ID/PW account");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    // 회원가입 (ID/PW 또는 소셜)
    public User signup(SignupRequest request) {
        // ID/PW 로그인인 경우, 아이디와 비밀번호가 필요
        if (request.getLoginType() == LoginType.idpw) {
            if (request.getUserId() == null || request.getPassword() == null) {
                throw new RuntimeException("User ID and password are required for ID/PW login");
            }
            // 아이디가 이미 존재하는 경우
            if (userRepository.findByUuid(UUID.fromString(request.getUserId())).isPresent()) {
                throw new RuntimeException("User ID already exists");
            }
        }
        // 소셜 로그인인 경우, 토큰이 필요
        else if (request.getLoginType() == LoginType.social) {
            if (request.getToken() == null) {
                throw new RuntimeException("Token is required for social login");
            }
            // 소셜 아이디가 이미 존재하는 경우
            if (userRepository.findBySocialId(request.getToken()).isPresent()) {
                throw new RuntimeException("Social account already exists");
            }
        }
        // 닉네임이 이미 존재하는 경우 -> 중복 닉네임 예외 발생
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new RuntimeException("Nickname already exists");
        }

        // User 엔티티 생성
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .loginType(request.getLoginType())
                .socialId(request.getToken())
                .passwordHash(request.getLoginType() == LoginType.idpw ?
                        passwordEncoder.encode(request.getPassword()) : null)
                .nickname(request.getNickname())
                .agreedTerms(request.isAgreedTerms())
                .build();

        // UserProfile 엔티티 생성
        UserProfile userProfile = UserProfile.builder()
                .user(user)
                .age(request.getAge())
                .gender(Gender.valueOf(request.getGender().toUpperCase()))
                .birthDate(LocalDate.parse(request.getBirthDate()))
                .height(new BigDecimal(request.getHeight()))
                .weight(new BigDecimal(request.getWeight()))
                .phone(request.getPhone())
                .build();

        // Interests 엔티티 생성, "interest": ["diet", "health", "muscle"] 형태
        Interests interests = Interests.builder()
                .user(user)
                .diet(request.getInterest().contains("diet"))
                .health(request.getInterest().contains("health"))
                .muscle(request.getInterest().contains("muscle"))
                .aging(request.getInterest().contains("aging"))
                .nutrient(request.getInterest().contains("nutrient"))
                .build();

        // 연관관계 설정
        user.setUserProfile(userProfile);
        user.setInterests(interests);

        return userRepository.save(user);
    }

    // 현재 로그인한 사용자 정보 조회
    public User getCurrentUser(User user) {
        return userRepository.findById(user.getUuid())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 이용약관 동의
    public User agreeToTerms(User user) {
        user.setAgreedTerms(true);
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
        user.setProfilePhotoUrl(photoUrl);
        return userRepository.save(user);
    }
}