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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {
    private final UserTokenRepository userTokenRepository;

    // * from .properties to TokenService
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity-in-minutes}")
    private long accessTokenValidityInMinutes;

    @Value("${jwt.refresh-token-validity-in-days}")
    private long refreshTokenValidityInDays;

    // * generateTokens
    public UserToken generateTokens(UUID userId) {
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

    public UserToken refreshTokens(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(refreshToken)
                .getBody();

        UUID userId = UUID.fromString(claims.getSubject());
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

    private String generateAccessToken(UUID userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidityInMinutes * 60 * 1000))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    private String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidityInDays * 24 * 60 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return UUID.fromString(claims.getSubject());
    }
} 