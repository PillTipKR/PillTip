// PillTip\BE\src\main\java\com\oauth2\repository\UserRepository.java
// author : mireutale
// date : 2025-05-19
// description : 사용자 리포지토리

package com.oauth2.repository;

import com.oauth2.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// DB에서 유저 조회
// JpaRepository를 상속받아 기본적인 CRUD 작업 제공
// save, findById, findAll, delete 등의 메서드 제공
public interface UserRepository extends JpaRepository<User, Long> {
    // SELECT * FROM users WHERE uuid = ?
    Optional<User> findById(Long id);
    // SELECT * FROM users WHERE nickname = ?
    Optional<User> findByNickname(String nickname);
    // SELECT * FROM users WHERE social_id = ?
    Optional<User> findBySocialId(String socialId);
    // SELECT * FROM users WHERE login_id = ?
    Optional<User> findByLoginId(String loginId);
}