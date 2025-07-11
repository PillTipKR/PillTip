// author : mireutale
// description : JWT 인증 필터
package com.oauth2.User.Auth.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Auth.Entity.UserToken;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.Auth.Repository.UserTokenRepository;
import com.oauth2.User.Auth.Service.TokenService;
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
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // JWT 토큰 검증이 필요 없는 경로들
        String requestURI = request.getRequestURI();
        System.out.println("Request URI: " + requestURI); // 디버깅 로그
        
        if (requestURI.equals("/api/auth/signup") || 
            requestURI.equals("/api/auth/login") || 
            requestURI.equals("/api/auth/check-duplicate") ||
            requestURI.equals("/api/auth/refresh") ||
            requestURI.startsWith("/oauth2/") ||
            requestURI.startsWith("/api/questionnaire/public/")) {
            System.out.println("Skipping JWT validation for: " + requestURI); // 디버깅 로그
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 요청 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);
            System.out.println("JWT Token: " + (jwt != null ? jwt.substring(0, Math.min(jwt.length(), 20)) + "..." : "null")); // 디버깅 로그

            // JWT 토큰이 유효한 경우 사용자 ID를 추출
            if (jwt != null) {
                try {
                    System.out.println("=== JWT TOKEN VALIDATION START ===");
                    
                    // 토큰에서 사용자 ID 추출
                    Long userId = tokenService.getUserIdFromToken(jwt);
                    System.out.println("User ID from token: " + userId); // 디버깅 로그
                    
                    // DB에서 토큰 정보 조회
                    UserToken userToken = userTokenRepository.findById(userId)
                            .orElseThrow(() -> new BadCredentialsException("유효하지 않은 토큰입니다."));
                    System.out.println("UserToken found in DB for user: " + userId);

                    // 토큰 일치 여부 및 만료 시간 검증
                    System.out.println("Stored access token: " + userToken.getAccessToken().substring(0, Math.min(userToken.getAccessToken().length(), 20)) + "...");
                    System.out.println("Request access token: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
                    System.out.println("Tokens match: " + userToken.getAccessToken().equals(jwt));
                    
                    if (!userToken.getAccessToken().equals(jwt)) {
                        System.out.println("Token mismatch detected");
                        throw new BadCredentialsException("유효하지 않은 토큰입니다.");
                    }

                    System.out.println("Access token expiry: " + userToken.getAccessTokenExpiry());
                    System.out.println("Current time: " + LocalDateTime.now());
                    System.out.println("Token expired: " + userToken.getAccessTokenExpiry().isBefore(LocalDateTime.now()));
                    
                    if (userToken.getAccessTokenExpiry().isBefore(LocalDateTime.now())) {
                        System.out.println("Token expired detected");
                        throw new BadCredentialsException("토큰이 만료되었습니다.");
                    }

                    // 사용자 정보 조회
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BadCredentialsException("사용자를 찾을 수 없습니다."));
                    
                    // 사용자 정보를 기반으로 인증 토큰 생성
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(user, null, null);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 인증 토큰을 컨텍스트에 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Authentication successful for user: " + user.getNickname()); // 디버깅 로그
                } catch (RuntimeException e) {
                    System.out.println("Authentication failed: " + e.getMessage()); // 디버깅 로그
                    SecurityContextHolder.clearContext();
                    throw new BadCredentialsException(e.getMessage());
                }
            }
            
            // 정상적인 경우에만 필터 체인 계속 실행
            filterChain.doFilter(request, response);
            
        } catch (BadCredentialsException ex) {
            SecurityContextHolder.clearContext();
            // 에러 응답 반환
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            
            String errorType = ex.getMessage().contains("만료") ? "expired" : "invalid";
            String errorMessage = ex.getMessage();
            
            // 더 구체적인 에러 메시지 제공
            if (errorMessage.contains("유효하지 않은 토큰")) {
                errorMessage = "토큰이 유효하지 않습니다. 다시 로그인해주세요.";
            } else if (errorMessage.contains("토큰이 만료")) {
                errorMessage = "토큰이 만료되었습니다. 토큰을 갱신하거나 다시 로그인해주세요.";
            } else if (errorMessage.contains("사용자를 찾을 수 없습니다")) {
                errorMessage = "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.";
            }
            
            response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error(errorMessage, errorType)
            ));
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage()); // 디버깅 로그
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error("인증에 실패했습니다. 토큰을 확인하고 다시 시도해주세요.", "invalid")
            ));
        }
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
