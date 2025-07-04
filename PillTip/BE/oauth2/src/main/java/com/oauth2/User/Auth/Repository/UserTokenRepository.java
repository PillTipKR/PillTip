// author : mireutale
// description : UserToken 엔티티를 위한 리포지토리 인터페이스.
package com.oauth2.User.Auth.Repository;

import com.oauth2.User.Auth.Entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByUserId(Long loginId);

    @Query("SELECT ut FROM UserToken ut WHERE ut.refreshToken = :refreshToken")
    Optional<UserToken> findByRefreshToken(@Param("refreshToken") String refreshToken);

    @Modifying
    @Query("UPDATE UserToken ut SET ut.accessToken = :accessToken, ut.accessTokenExpiry = :accessTokenExpiry WHERE ut.userId = :userId")
    void updateAccessToken(@Param("accessToken") String accessToken, @Param("accessTokenExpiry") LocalDateTime accessTokenExpiry);
}
