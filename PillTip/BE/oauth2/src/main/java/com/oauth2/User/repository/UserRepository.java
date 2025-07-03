// author : mireutale
// description : 유저 정보 저장소
package com.oauth2.User.repository;

import com.oauth2.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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

    // DrugAlarmScheduler에서 사용하는 쿼리
    // 복용 중인 약이 있는 유저 조회, 로그인 상태인 유저만 조회, 성능 최적화를 위해 DISTINCT 및 JOIN FETCH 사용
    @Query("""
    SELECT DISTINCT u FROM User u
    JOIN FETCH u.FCMToken t
    JOIN FETCH u.userProfile p
    JOIN u.takingPills tp
    WHERE t.loggedIn = true
    """)
    List<User> findAllActiveUsersWithPillInfo();

    // 현재 사용자 정보 조회 (questionnaires 포함)
    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.questionnaires
    WHERE u.id = :userId
    """)
    Optional<User> findByIdWithQuestionnaires(Long userId);
}
