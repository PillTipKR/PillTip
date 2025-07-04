// author : mireutale
// description : 유저 동의 저장소
package com.oauth2.User.UserInfo.Repository;

import com.oauth2.User.UserInfo.Entity.UserPermissions;
import com.oauth2.User.Auth.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPermissionsRepository extends JpaRepository<UserPermissions, Long> {
    // SELECT * FROM user_permissions WHERE user_id = ?
    Optional<UserPermissions> findByUser(User user);
    
    // SELECT * FROM user_permissions WHERE user_id = ?
    Optional<UserPermissions> findByUserId(Long userId);
} 