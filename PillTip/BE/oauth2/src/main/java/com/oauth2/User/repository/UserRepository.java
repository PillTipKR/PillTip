// author : mireutale
// description : 유저 정보 저장소
package com.oauth2.User.repository;

import com.oauth2.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// DB에서 User table 접근
// JpaRepository를 상속받아 기본적인 CRUD 작업 제공 -> SQL을 사용하지 않고, 메서드를 상속받아서 DB와 상호작용
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
