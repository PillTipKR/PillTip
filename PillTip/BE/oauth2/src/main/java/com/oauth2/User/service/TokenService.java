// author : mireutale
// description : JWT 토큰 관리를 위한 서비스 클래스
package com.oauth2.User.service;

import com.oauth2.User.entity.UserToken;
import com.oauth2.User.repository.UserTokenRepository;
import com.oauth2.User.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * JWT 토큰 관리를 위한 서비스 클래스
 * 액세스 토큰과 리프레시 토큰의 생성, 검증, 갱신을 담당
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {
    private final UserTokenRepository userTokenRepository;
    private final UserRepository userRepository;

    // JWT 시크릿 키 (application.properties에서 주입)
    @Value("${jwt.secret}")
    private String secretKey;

    // 액세스 토큰 유효 시간 (분 단위)
    @Value("${jwt.access-token-validity-in-minutes}")
    private long accessTokenValidityInMinutes;

    // 리프레시 토큰 유효 시간 (일 단위)
    @Value("${jwt.refresh-token-validity-in-days}")
    private long refreshTokenValidityInDays;

    /**
     * 새로운 액세스 토큰과 리프레시 토큰을 생성
     * @param userId 사용자 ID
     * @return 생성된 토큰 정보를 담은 UserToken 객체
     */
    public UserToken generateTokens(Long userId) {
        String accessToken = generateAccessToken(userId);
        String refreshToken = generateRefreshToken(userId);
        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(accessTokenValidityInMinutes);
        LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(refreshTokenValidityInDays);

        // 기존 토큰이 있는지 확인
        UserToken existingToken = userTokenRepository.findById(userId).orElse(null);
        
        if (existingToken != null) {
            // 기존 토큰이 있으면 업데이트
            existingToken.updateTokens(accessToken, refreshToken, accessTokenExpiry, refreshTokenExpiry);
            return userTokenRepository.save(existingToken);
        } else {
            // 기존 토큰이 없으면 새로 생성
            UserToken newToken = UserToken.builder()
                    .userId(userId)
                    .user(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")))
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiry(accessTokenExpiry)
                    .refreshTokenExpiry(refreshTokenExpiry)
                    .build();
            return userTokenRepository.save(newToken);
        }
    }

    // 토큰 갱신 결과 클래스
    public static class TokenRefreshResult {
        private final UserToken userToken; // 갱신된 토큰 정보
        private final boolean isRefreshTokenRenewed; // 리프레시 토큰 갱신 여부

        // 생성자
        public TokenRefreshResult(UserToken userToken, boolean isRefreshTokenRenewed) {
            this.userToken = userToken;
            this.isRefreshTokenRenewed = isRefreshTokenRenewed;
        }

        // 갱신된 토큰 정보 반환
        public UserToken getUserToken() {
            return userToken;
        }

        // 리프레시 토큰 갱신 여부 반환
        public boolean isRefreshTokenRenewed() {
            return isRefreshTokenRenewed;
        }
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급
     * @param refreshToken 기존 리프레시 토큰
     * @return 갱신된 토큰 정보를 담은 UserToken 객체
     * @throws BadCredentialsException 토큰이 유효하지 않거나 찾을 수 없는 경우
     */
    public TokenRefreshResult refreshTokens(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!validateToken(refreshToken)) {
            throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 리프레시 토큰에서 사용자 ID 추출
        Claims claims = getClaimsFromToken(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());
        
        // 사용자 ID로 토큰 조회
        UserToken userToken = userTokenRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("토큰을 찾을 수 없습니다."));

        // 리프레시 토큰 일치 여부 검증
        if (!userToken.getRefreshToken().equals(refreshToken)) {
            throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
        }

        // Refresh Token이 만료된 경우 새로운 토큰 발급
        if (userToken.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            // 만료된 경우: 새로운 토큰 발급
            UserToken newTokens = generateTokens(userId);
            return new TokenRefreshResult(newTokens, true);
        }

        // Access Token만 갱신
        String newAccessToken = generateAccessToken(userId);
        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(accessTokenValidityInMinutes);
        userToken.updateAccessToken(newAccessToken, accessTokenExpiry);
        return new TokenRefreshResult(userTokenRepository.save(userToken), false);
    }

    /**
     * 액세스 토큰 생성
     * @param userId 사용자 ID
     * @return 생성된 액세스 토큰
     */
    private String generateAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidityInMinutes * 60 * 1000))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    /**
     * 리프레시 토큰 생성
     * @param userId 사용자 ID
     * @return 생성된 리프레시 토큰
     */
    private String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidityInDays * 24 * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    /**
     * 리프레시 토큰의 유효성을 검증
     * @param token 검증할 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     * @throws BadCredentialsException 토큰이 만료되었거나 유효하지 않은 경우
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("토큰이 만료되었습니다.");
        } catch (Exception e) {
            throw new BadCredentialsException("유효하지 않은 토큰입니다.");
        }
    }

    /**
     * 토큰에서 Claims를 추출
     * @param token JWT 토큰
     * @return 토큰의 Claims
     * @throws BadCredentialsException 토큰이 유효하지 않은 경우
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
            } catch (ExpiredJwtException e) {
                throw new BadCredentialsException("토큰이 만료되었습니다.");
            } catch (Exception e) {
                throw new BadCredentialsException("유효하지 않은 토큰입니다.");
            }
    }

    /**
     * 토큰에서 사용자 ID를 추출
     * @param token JWT 토큰
     * @return 토큰에 포함된 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }

    // UserToken 엔티티에 Access Token만 업데이트하는 메서드 추가
    public void updateAccessToken(String accessToken, LocalDateTime accessTokenExpiry) {
        userTokenRepository.updateAccessToken(accessToken, accessTokenExpiry);
    }
}
