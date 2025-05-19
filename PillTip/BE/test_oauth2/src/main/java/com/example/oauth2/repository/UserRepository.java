// PillTip\BE\src\main\java\com\example\oauth2\repository\UserRepository.java
// author : mireutale
// date : 2025-05-19
// description : 사용자 리포지토리

package com.example.oauth2.repository;

import com.example.oauth2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// DB에서 유저 조회
// CustomOAuth2UserService에서 return으로 user의 정보를 DB에 저장하기 위해서 사용
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email); // 이메일로 유저 조회
    Optional<User> findByNickname(String nickname);
    Optional<User> findByUserId(String userId);
    Optional<User> findBySocialId(String socialId);
}