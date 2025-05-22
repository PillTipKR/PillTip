package com.oauth2.service;

import com.oauth2.entity.UserToken;
import com.oauth2.repository.UserTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        UserToken userToken = UserToken.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiry(accessTokenExpiry)
                .refreshTokenExpiry(refreshTokenExpiry)
                .build();

        return userTokenRepository.save(userToken);
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급
     * @param refreshToken 기존 리프레시 토큰
     * @return 갱신된 토큰 정보를 담은 UserToken 객체
     * @throws RuntimeException 토큰이 유효하지 않거나 찾을 수 없는 경우
     */
    public UserToken refreshTokens(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(refreshToken)
                .getBody();

        Long userId = Long.parseLong(claims.getSubject());
        UserToken userToken = userTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        if (!userToken.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = generateAccessToken(userId);
        String newRefreshToken = generateRefreshToken(userId);
        
        LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(accessTokenValidityInMinutes);
        LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(refreshTokenValidityInDays);

        userToken.updateTokens(newAccessToken, newRefreshToken, accessTokenExpiry, refreshTokenExpiry);
        return userTokenRepository.save(userToken);
    }

    /**
     * 액세스 토큰 생성
     * @param userId 사용자 ID
     * @return 생성된 액세스 토큰
     */
    private String generateAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
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
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidityInDays * 24 * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    /**
     * 토큰의 유효성을 검증
     * @param token 검증할 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰에서 사용자 ID를 추출
     * @param token JWT 토큰
     * @return 토큰에 포함된 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
} 