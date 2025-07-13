// author : mireutale
// description : API 컨트롤러

package com.oauth2.User.Auth.Controller;

import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Dto.LoginRequest;
import com.oauth2.User.Auth.Dto.SocialLoginRequest;
import com.oauth2.User.Auth.Dto.SignupRequest;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Auth.Service.LoginService;
import com.oauth2.User.UserInfo.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.oauth2.User.Auth.Dto.LoginResponse;
import com.oauth2.User.Auth.Service.SignupService;
import com.oauth2.User.Auth.Entity.UserToken;
import com.oauth2.User.Auth.Service.TokenService;
import com.oauth2.User.Auth.Dto.SignupResponse;
import com.oauth2.User.Auth.Dto.DuplicateCheckRequest;
import com.oauth2.User.Auth.Dto.TermsResponse;
import com.oauth2.User.Alarm.Repository.FCMTokenRepository;
import com.oauth2.User.Auth.Dto.AuthMessageConstants;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SignupService signupService;
    private final TokenService tokenService;
    private final FCMTokenRepository fcmTokenRepository;
    private final LoginService loginService;

    // ID/PW 로그인 또는 소셜 로그인 (자동 감지)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        System.out.println("=== 로그인 API 호출 ===");
        System.out.println("LoginId: " + request.loginId());
        System.out.println("Password: " + (request.password() != null ? "***" : "null"));
        
        try {
            // loginId가 null이고 password가 null이면 소셜 로그인으로 간주
            if (request.loginId() == null && request.password() == null) {
                System.out.println("ERROR: 소셜 로그인 요청이 /api/auth/login으로 전송되었습니다. /api/auth/social-login을 사용해주세요.");
                return ResponseEntity.status(400)
                    .body(ApiResponse.error("소셜 로그인은 /api/auth/social-login API를 사용해주세요.", null));
            } else {
                // 일반 ID/PW 로그인
                System.out.println("일반 ID/PW 로그인 처리 중...");
                LoginResponse loginResponse = loginService.login(request);
                System.out.println("로그인 성공!");
                return ResponseEntity.status(200)
                    .body(ApiResponse.success(AuthMessageConstants.LOGIN_SUCCESS, loginResponse));
            }
        } catch (Exception e) {
            System.out.println("로그인 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400)
                .body(ApiResponse.error(AuthMessageConstants.LOGIN_FAILED, null));
        }
    }

    // 소셜 로그인
    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<LoginResponse>> socialLogin(@RequestBody SocialLoginRequest request) {
        System.out.println("=== 소셜 로그인 API 호출됨! ===");
        System.out.println("Provider: " + request.getProvider());
        System.out.println("Token: " + (request.getToken() != null ? request.getToken().substring(0, Math.min(10, request.getToken().length())) + "..." : "null"));
        System.out.println("전체 요청: " + request.toString());
        System.out.println("요청 타임스탬프: " + System.currentTimeMillis());
        
        try {
            System.out.println("소셜 로그인 처리 중...");
            LoginResponse loginResponse = loginService.socialLogin(request);
            System.out.println("소셜 로그인 성공 - Provider: " + request.getProvider());
            return ResponseEntity.status(200)
                .body(ApiResponse.success(AuthMessageConstants.SOCIAL_LOGIN_SUCCESS, loginResponse));
        } catch (Exception e) {
            System.out.println("소셜 로그인 실패 - Provider: " + request.getProvider() + ", Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400)
                .body(ApiResponse.error(AuthMessageConstants.LOGIN_FAILED, null));
        }
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@RequestBody SignupRequest request) {
        System.out.println("=== 회원가입 API 호출 ===");
        System.out.println("LoginType: " + request.getLoginType());
        System.out.println("Provider: " + request.getProvider());
        System.out.println("Nickname: " + request.getNickname());
        
        try {
            System.out.println("SignupService.signup() 호출 시작");
            User user = signupService.signup(request);
            System.out.println("SignupService.signup() 완료 - UserId: " + user.getId());
            
            System.out.println("토큰 생성 시작");
            UserToken userToken = tokenService.generateTokens(user.getId());
            System.out.println("토큰 생성 완료");
            
            SignupResponse signupResponse = SignupResponse.builder()
                    .accessToken(userToken.getAccessToken())
                    .refreshToken(userToken.getRefreshToken())
                    .build();
            
            System.out.println("회원가입 성공 - UserId: " + user.getId() + ", LoginType: " + user.getLoginType());
            return ResponseEntity.status(201)
                .body(ApiResponse.success(AuthMessageConstants.SIGNUP_SUCCESS, signupResponse));
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            System.out.println("회원가입 실패 - Error: " + errorMessage);
            
            if (errorMessage.contains(AuthMessageConstants.ERROR_KEYWORD_LOGIN_TYPE)) {
                System.out.println("로그인 타입 관련 에러");
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.LOGIN_TYPE_REQUIRED, null));
            } else if (errorMessage.contains(AuthMessageConstants.ERROR_KEYWORD_PHONE_NUMBER)) {
                System.out.println("전화번호 관련 에러");
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.DUPLICATE_PHONE_FORMAT, null));
            } else if (errorMessage.contains(AuthMessageConstants.ERROR_KEYWORD_NICKNAME)) {
                System.out.println("닉네임 관련 에러");
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.DUPLICATE_NICKNAME_FORMAT, null));
            } else if (errorMessage.contains(AuthMessageConstants.ERROR_KEYWORD_USER_ID)) {
                System.out.println("사용자 ID 관련 에러");
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.DUPLICATE_LOGIN_ID, null));
            } else {
                System.out.println("기타 회원가입 에러: " + errorMessage);
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.SIGNUP_FAILED, null));
            }
        } catch (Exception e) {
            System.out.println("예상치 못한 회원가입 에러: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(ApiResponse.error(AuthMessageConstants.SIGNUP_FAILED, null));
        }
    }

    // 중복 체크 API
    @PostMapping("/check-duplicate")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicate(@RequestBody DuplicateCheckRequest request) {
        boolean isDuplicate = userService.isDuplicate(request.value(), request.type());
        String message = isDuplicate ? 
            String.format(AuthMessageConstants.DUPLICATE_CHECK_FAILED, request.type()) : 
            String.format(AuthMessageConstants.DUPLICATE_CHECK_SUCCESS, request.type());
        return ResponseEntity.status(200)
            .body(ApiResponse.success(message, !isDuplicate));
    }

    // ------------------------------------------------------------ jwt 토큰 필요 ------------------------------------------------------------
    // 로그아웃
    @PutMapping("/logout")
    public ResponseEntity<ApiResponse<String>> Logout(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(AuthMessageConstants.USER_NOT_AUTHENTICATED, null));
        }

        try {
            User currentUser = userService.getCurrentUser(user.getId());
            
            if (currentUser.getFCMToken() != null) {
                currentUser.getFCMToken().setLoggedIn(false);
                fcmTokenRepository.save(currentUser.getFCMToken());
            }
            
            return ResponseEntity.status(200)
                    .body(ApiResponse.success(AuthMessageConstants.LOGOUT_SUCCESS));
        } catch (Exception e) {
            System.out.println("Error during logout for user: " + user.getId() + " - Error: " + e.getMessage());
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(AuthMessageConstants.LOGOUT_FAILED, null));
        }
    }

    // 토큰 갱신 API
    @PostMapping("/refresh")
    // 헤더에 Refresh-Token 헤더가 있는 경우 토큰 갱신
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        try {
            LoginResponse loginResponse = loginService.refreshToken(refreshToken);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(AuthMessageConstants.TOKEN_REFRESH_SUCCESS, loginResponse));
        } catch (RuntimeException e) {
            System.out.println("Token refresh failed: " + e.getMessage());
            return ResponseEntity.status(400)
                .body(ApiResponse.error(AuthMessageConstants.TOKEN_REFRESH_FAILED, null));
        }
    }

    // 이용약관 동의
    @PostMapping("/terms")
    public ResponseEntity<ApiResponse<TermsResponse>> agreeToTerms(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error(AuthMessageConstants.USER_NOT_AUTHENTICATED, null));
        }
        
        try {
            User updatedUser = userService.agreeToTerms(user);
            TermsResponse termsResponse = TermsResponse.builder()
                .terms(updatedUser.isTerms())
                .nickname(updatedUser.getNickname())
                .build();
            return ResponseEntity.status(200)
                .body(ApiResponse.success(AuthMessageConstants.TERMS_AGREEMENT_SUCCESS, termsResponse));
        } catch (Exception e) {
            System.out.println("Terms agreement failed: " + e.getMessage());
            return ResponseEntity.status(400)
                .body(ApiResponse.error(AuthMessageConstants.TERMS_AGREEMENT_FAILED, null));
        }
    }
}
