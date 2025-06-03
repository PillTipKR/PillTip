// author : mireutale
// description : JWT 인증 필터
package com.oauth2.User.security;

import com.oauth2.User.entity.User;
import com.oauth2.User.repository.UserRepository;
import com.oauth2.User.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 요청 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);

            // JWT 토큰이 유효한 경우 사용자 ID를 추출
            if (jwt != null && tokenService.validateToken(jwt)) {
                Long userId = tokenService.getUserIdFromToken(jwt);
                
                // 사용자 ID가 존재하는 경우 사용자 정보를 조회
                if (userId != null) {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    
                    // 사용자 정보를 기반으로 인증 토큰 생성
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(user, null, null);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 인증 토큰을 컨텍스트에 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    /*
     * 요청 헤더에서 JWT 토큰 추출
     * Authorization 헤더에서 Bearer 토큰을 추출하고, 토큰 값을 반환
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Authorization 헤더에서 Bearer 토큰을 추출
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Bearer 토큰 값 반환
        }
        return null; // 토큰이 없는 경우 null 반환
    }
} 