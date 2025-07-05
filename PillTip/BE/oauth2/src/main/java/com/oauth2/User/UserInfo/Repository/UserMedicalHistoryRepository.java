// author : mireutale
// description : 사용자 의료 이력 Repository
package com.oauth2.User.UserInfo.Repository;

import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.UserInfo.Entity.UserSensitiveInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMedicalHistoryRepository extends JpaRepository<UserSensitiveInfo, Long> {
    
    /**
     * 사용자별 의료 이력 조회
     */
    Optional<UserSensitiveInfo> findByUser(User user);
    
    /**
     * 사용자 ID로 의료 이력 조회
     */
    Optional<UserSensitiveInfo> findByUserId(Long userId);
    
    /**
     * 사용자별 의료 이력 존재 여부 확인
     */
    boolean existsByUser(User user);
    
    /**
     * 사용자 ID로 의료 이력 존재 여부 확인
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 사용자별 의료 이력 삭제
     */
    void deleteByUser(User user);
    
    /**
     * 사용자 ID로 의료 이력 삭제
     */
    void deleteByUserId(Long userId);
} 