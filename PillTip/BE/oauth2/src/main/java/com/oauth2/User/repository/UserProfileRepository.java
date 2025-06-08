// author : mireutale
// description : 유저 프로필 저장소
package com.oauth2.User.repository;

import com.oauth2.User.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    // SELECT * FROM user_profile WHERE phone = ?
    Optional<UserProfile> findByPhone(String phone);
    
    // SELECT * FROM user_profile WHERE user_id = ?
    Optional<UserProfile> findByUserId(Long userId);
}
