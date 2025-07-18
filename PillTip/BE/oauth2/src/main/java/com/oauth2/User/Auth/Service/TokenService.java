// author : mireutale
// description : JWT 토큰 관리를 위한 서비스 클래스
package com.oauth2.User.Auth.Service;

import com.oauth2.User.Auth.Entity.UserToken;
import com.oauth2.User.Auth.Repository.UserTokenRepository;
import com.oauth2.User.Auth.Repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.BadCredentialsException;
import io.jsonwebtoken.SignatureAlgorithm;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.util.Date;
import com.oauth2.User.Auth.Dto.AuthMessageConstants;

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
                    .user(userRepository.findById(userId).orElseThrow(() -> new RuntimeException(AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN)))
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
            throw new BadCredentialsException(AuthMessageConstants.INVALID_REFRESH_TOKEN_DETAIL);
        }

        // 리프레시 토큰에서 사용자 ID 추출
        Claims claims = getClaimsFromToken(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());
        
        // 사용자 ID로 토큰 조회
        UserToken userToken = userTokenRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException(AuthMessageConstants.TOKEN_NOT_FOUND));

        // 리프레시 토큰 일치 여부 검증
        if (!userToken.getRefreshToken().equals(refreshToken)) {
            throw new BadCredentialsException(AuthMessageConstants.INVALID_REFRESH_TOKEN_DETAIL);
        }

        // 보안을 위해 매번 새로운 토큰 발급 (리프레시 토큰 만료 여부와 관계없이)
        UserToken newTokens = generateTokens(userId);
        return new TokenRefreshResult(newTokens, true);
    }

    /**
     * 액세스 토큰 생성
     * @param userId 사용자 ID
     * @return 생성된 액세스 토큰
     */
    private String generateAccessToken(Long userId) {
        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidityInMinutes * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }   

    /**
     * 리프레시 토큰 생성
     * @param userId 사용자 ID
     * @return 생성된 리프레시 토큰
     */
    private String generateRefreshToken(Long userId) {
        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidityInDays * 24 * 60 * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
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
            Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException(AuthMessageConstants.TOKEN_EXPIRED_DETAIL);
        } catch (Exception e) {
            throw new BadCredentialsException(AuthMessageConstants.INVALID_TOKEN);
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
            return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
            } catch (ExpiredJwtException e) {
                throw new BadCredentialsException(AuthMessageConstants.TOKEN_EXPIRED_DETAIL);
            } catch (Exception e) {
                throw new BadCredentialsException(AuthMessageConstants.INVALID_TOKEN);
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

    // 커스텀 JWT 토큰 생성 (userId, questionnaireId, hospitalCode, 만료초)
    public String createCustomJwtToken(Long userId, String hospitalCode, int expiresInSeconds) {
        long now = System.currentTimeMillis();
        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("hospitalCode", hospitalCode)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiresInSeconds * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)    
                .compact();

        return token;
    }

    // 커스텀 JWT 토큰 검증 (questionnaireId 일치)
    public boolean validateCustomJwtToken(String token, String id) {
        try {
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
            String tokenId = claims.get("id", String.class);
            if (!id.equals(tokenId)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // QR URL용 JWT 토큰 검증 (userId 일치)
    public boolean validateQRJwtToken(String token, Long userId) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
            Long tokenUserId = Long.parseLong(claims.getSubject());
            return userId.equals(tokenUserId);
        } catch (Exception e) {
            return false;
        }
    }

    // 친구 추가용 jwt 생성
    public String createFriendInviteToken(Long inviterId, int expiresInSeconds) {
        Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
        return Jwts.builder()
                .setSubject("friend-invite")
                .claim("inviterId", inviterId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiresInSeconds * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 검증 코드
    public Long getInviterIdFromFriendToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("inviterId", Long.class);
    }
}
