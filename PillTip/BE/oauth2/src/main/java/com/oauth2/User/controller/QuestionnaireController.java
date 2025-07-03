// author : mireutale
// description : 문진표 관련 API 컨트롤러
package com.oauth2.User.controller;

import com.oauth2.User.dto.ApiResponse;
import com.oauth2.User.dto.UserPermissionsRequest;
import com.oauth2.User.dto.UserPermissionsResponse;
import com.oauth2.User.entity.User;
import com.oauth2.User.service.UserPermissionsService;
import com.oauth2.User.dto.PatientQuestionnaireRequest;
import com.oauth2.User.dto.PatientQuestionnaireResponse;
import com.oauth2.User.entity.PatientQuestionnaire;
import com.oauth2.User.service.PatientQuestionnaireService;
import com.oauth2.User.service.UserService;
import com.oauth2.User.dto.QuestionnaireAvailabilityResponse;
import com.oauth2.User.dto.QRQuestionnaireResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.User.service.TokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import com.oauth2.User.service.HospitalService;

@RestController
@RequestMapping("/api/questionnaire")
@RequiredArgsConstructor
public class QuestionnaireController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireController.class);
    private final UserPermissionsService userPermissionsService;
    private final PatientQuestionnaireService patientQuestionnaireService;
    private final TokenService tokenService;
    private final HospitalService hospitalService;
    private final UserService userService;
    //동의사항 조회
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<UserPermissionsResponse>> getUserPermissions(
            @AuthenticationPrincipal User user) {
        logger.info("Received getUserPermissions request for user: {}", user.getId());
        
        try {
            UserPermissionsResponse permissions = userPermissionsService.getUserPermissions(user);
            logger.info("Successfully retrieved permissions for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Permissions retrieved successfully", permissions));
        } catch (Exception e) {
            logger.error("Error retrieving permissions for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to retrieve permissions: " + e.getMessage(), null));
        }
    }
  
    //동의사항 수정
    @PutMapping("/permissions/multi")
    public ResponseEntity<ApiResponse<UserPermissionsResponse>> updateMedicalPermissions(
            @AuthenticationPrincipal User user,
            @RequestBody UserPermissionsRequest request) {
        logger.info("Received updateMedicalPermissions request for user: {}", user.getId());
        logger.debug("UserPermissionsRequest details: {}", request);
        
        try {
            UserPermissionsResponse permissions = userPermissionsService.updateMedicalPermissions(user, request);
            logger.info("Successfully updated medical permissions for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Medical permissions updated successfully", permissions));
        } catch (Exception e) {
            logger.error("Error updating medical permissions for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to update medical permissions: " + e.getMessage(), null));
        }
    }
    //동의사항 수정 (여러개 수정)
    @PutMapping("/permissions/{permissionType}")
    public ResponseEntity<ApiResponse<UserPermissionsResponse>> updatePermission(
            @AuthenticationPrincipal User user,
            @PathVariable String permissionType,
            @RequestParam boolean granted) {
        logger.info("Received updatePermission request for user: {} - Type: {}, Granted: {}", 
                   user.getId(), permissionType, granted);
        
        try {
            UserPermissionsResponse permissions = userPermissionsService.updatePermission(user, permissionType, granted);
            logger.info("Successfully updated permission for user: {} - Type: {}", user.getId(), permissionType);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Permission updated successfully", permissions));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid permission type for user: {} - Type: {}", user.getId(), permissionType);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Invalid permission type: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error updating permission for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to update permission: " + e.getMessage(), null));
        }
    }
    //문진표 기능 사용 가능 여부 확인
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<QuestionnaireAvailabilityResponse>> isQuestionnaireAvailable(
            @AuthenticationPrincipal User user) {
        logger.info("Received isQuestionnaireAvailable request for user: {}", user.getId());
        
        try {
            // 1. 동의사항 확인
            UserPermissionsResponse permissions = userPermissionsService.getUserPermissions(user);
            boolean permissionsValid = permissions.isSensitiveInfoPermission() && permissions.isMedicalInfoPermission();
            
            // 2. 실명과 주소 확인
            boolean personalInfoValid = user.getRealName() != null && !user.getRealName().trim().isEmpty() &&
                                      user.getAddress() != null && !user.getAddress().trim().isEmpty();
            
            // 3. 모든 조건 확인
            boolean isAvailable = permissionsValid && personalInfoValid;
            
            // 4. 누락된 항목 수집
            java.util.List<String> missingItems = new java.util.ArrayList<>();
            if (!permissionsValid) {
                missingItems.add("동의사항 미완료");
            }
            if (!personalInfoValid) {
                missingItems.add("실명/주소 미입력");
            }
            
            // 5. 메시지 생성
            String message;
            if (isAvailable) {
                message = "문진표 작성이 가능합니다.";
            } else {
                message = "문진표 작성을 위해 다음 항목을 완료해주세요: " + String.join(", ", missingItems);
            }
            
            logger.info("Questionnaire availability check for user: {} - Permissions valid: {}, Personal info valid: {}, Available: {}", 
                user.getId(), permissionsValid, personalInfoValid, isAvailable);
            
            if (!isAvailable) {
                logger.info("Questionnaire not available for user: {} - Missing: {}", user.getId(), String.join(", ", missingItems));
            }
            
            QuestionnaireAvailabilityResponse response = QuestionnaireAvailabilityResponse.builder()
                .available(isAvailable)
                .permissionsValid(permissionsValid)
                .personalInfoValid(personalInfoValid)
                .missingItems(missingItems)
                .message(message)
                .build();
            
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Questionnaire availability checked successfully", response));
        } catch (Exception e) {
            logger.error("Error checking questionnaire availability for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to check questionnaire availability: " + e.getMessage(), null));
        }
    }

    // ---------------------------------------문진표---------------------------------------
    // 문진표 리스트 조회 (구체적인 경로를 먼저 정의)
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<java.util.List<com.oauth2.User.dto.PatientQuestionnaireSummaryResponse>>> getUserQuestionnaireList(
            @AuthenticationPrincipal User user) {
        java.util.List<com.oauth2.User.dto.PatientQuestionnaireSummaryResponse> list = patientQuestionnaireService.getUserQuestionnaireSummaries(user);
        return ResponseEntity.status(200)
            .body(ApiResponse.success("문진표 리스트 조회 성공", list));
    }
    
    // 문진표 작성
    @PostMapping("")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> createQuestionnaire(
            @AuthenticationPrincipal User user,
            @RequestBody PatientQuestionnaireRequest request) {
        // Validate realName and address
        if (request.getRealName() == null || request.getRealName().trim().isEmpty() ||
            request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("실명과 주소는 필수입니다.", null));
        }
        logger.info("Received createQuestionnaire request for user: {}", user.getId());
        try {
            PatientQuestionnaire questionnaire = patientQuestionnaireService.createQuestionnaire(user, request);
            PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire);
            logger.info("Successfully created questionnaire for user: {}", user.getId());
            return ResponseEntity.status(201)
                .body(ApiResponse.success("Questionnaire created successfully", response));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing questionnaire info for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to serialize questionnaire info: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error creating questionnaire for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to create questionnaire: " + e.getMessage(), null));
        }
    }
    
    // 문진표 상세 조회 (숫자 ID만 허용, 커스텀 토큰도 허용)
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> getQuestionnaireById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id,
            @RequestParam(value = "jwtToken", required = false) String jwtToken,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        logger.info("Received getQuestionnaireById request for user: {} - Questionnaire ID: {}", user != null ? user.getId() : null, id);
        try {
            // 1. 커스텀 토큰(문진표 열람용) 우선 검증
            String token = jwtToken;
            if ((token == null || token.isEmpty()) && authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }
            if (token != null && !token.isEmpty()) {
                logger.info("Trying custom token validation for questionnaireId: {}, token: {}", id, token);
                boolean valid = tokenService.validateCustomJwtToken(token, id);
                logger.info("Custom token validation result for questionnaireId {}: {}", id, valid);
                if (valid) {
                    PatientQuestionnaire questionnaire = patientQuestionnaireService.getQuestionnaireByIdPublic(id);
                    PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire);
                    logger.info("Successfully retrieved questionnaire by custom token - Questionnaire ID: {}", id);
                    return ResponseEntity.status(200)
                        .body(ApiResponse.success("문진표 조회 성공 (커스텀 토큰)", response));
                } else {
                    logger.warn("Custom token validation failed for questionnaireId: {}", id);
                }
            }
            // 2. 일반 사용자 인증 (기존 방식)
            if (user == null) {
                logger.warn("User not authenticated and no valid custom token");
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("인증이 필요합니다.", null));
            }
            PatientQuestionnaire questionnaire = patientQuestionnaireService.getQuestionnaireById(user, id);
            PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire);
            logger.info("Successfully retrieved questionnaire for user: {} - Questionnaire ID: {}", user.getId(), id);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("문진표 조회 성공", response));
        } catch (IllegalArgumentException e) {
            logger.error("Questionnaire not found for user: {} - Questionnaire ID: {} - Error: {}", user != null ? user.getId() : null, id, e.getMessage());
            return ResponseEntity.status(404)
                .body(ApiResponse.error("문진표를 찾을 수 없습니다: " + e.getMessage(), null));
        } catch (SecurityException e) {
            logger.error("Access denied for user: {} - Questionnaire ID: {} - Error: {}", user != null ? user.getId() : null, id, e.getMessage());
            return ResponseEntity.status(403)
                .body(ApiResponse.error("접근 권한이 없습니다: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error retrieving questionnaire for user: {} - Questionnaire ID: {} - Error: {}", user != null ? user.getId() : null, id, e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("문진표 조회 실패: " + e.getMessage(), null));
        }
    }
    // 문진표 삭제 (숫자 ID만 허용)
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<java.util.List<com.oauth2.User.dto.PatientQuestionnaireSummaryResponse>>> deleteQuestionnaire(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id) {
        java.util.List<com.oauth2.User.dto.PatientQuestionnaireSummaryResponse> list = patientQuestionnaireService.deleteQuestionnaireAndReturnList(user, id);
        return ResponseEntity.status(200)
            .body(ApiResponse.success("문진표 삭제 성공", list));
    }
    // 문진표 수정 (숫자 ID만 허용)
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> updateQuestionnaire(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id,
            @RequestBody PatientQuestionnaireRequest request) {
        // Validate realName and address
        if (request.getRealName() == null || request.getRealName().trim().isEmpty() ||
            request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("실명과 주소는 필수입니다.", null));
        }
        try {
            PatientQuestionnaire updated = patientQuestionnaireService.updateQuestionnaire(user, id, request);
            PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(updated);
            return ResponseEntity.ok(ApiResponse.success("문진표 수정 성공", response));
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to serialize questionnaire info: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to update questionnaire: " + e.getMessage(), null));
        }
    }
    // QR 코드를 통한 문진표 URL 생성 API (문진표 ID 명시)
    @PostMapping("/qr-url/{hospitalCode}/{questionnaireId}")
    public ResponseEntity<ApiResponse<QRQuestionnaireResponse>> generateQRQuestionnaireUrl(
            @AuthenticationPrincipal User user,
            @PathVariable String hospitalCode,
            @PathVariable Integer questionnaireId) {
        logger.info("=== QR QUESTIONNAIRE URL GENERATION START ===");
        logger.info("User ID: {}, Hospital Code: {}, Questionnaire ID: {}", user.getId(), hospitalCode, questionnaireId);
        
        try {
            // 1. 병원 코드 유효성 검사
            if (!hospitalService.existsByHospitalCode(hospitalCode)) {
                logger.warn("Invalid hospital code: {}", hospitalCode);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("존재하지 않는 병원 코드입니다.", null));
            }

            // 2. 해당 문진표가 본인 소유인지 검증
            PatientQuestionnaire questionnaire = patientQuestionnaireService.getQuestionnaireById(user, questionnaireId);
            if (questionnaire == null) {
                logger.warn("Questionnaire not found for user: {} - Questionnaire ID: {}", user.getId(), questionnaireId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("등록된 문진표가 없습니다.", null));
            }

            // 3. 5분(300초) 유효한 JWT 토큰 생성
            String jwtToken = tokenService.createCustomJwtToken(
                user.getId(),
                questionnaire.getQuestionnaireId(),
                hospitalCode,
                300 // 5분
            );

            // 4. URL 생성 (로컬 테스트용)
            String questionnaireUrl = String.format("http://localhost:3000/questionnaire/public/%d?jwtToken=%s",
                    questionnaire.getQuestionnaireId(), jwtToken);

            // 5. 응답 생성
            QRQuestionnaireResponse response = QRQuestionnaireResponse.builder()
                .questionnaireUrl(questionnaireUrl)
                .patientName(user.getRealName() != null ? user.getRealName() : user.getNickname())
                .patientPhone(user.getUserProfile().getPhone())
                .hospitalCode(hospitalCode)
                .questionnaireId(questionnaire.getQuestionnaireId())
                .accessToken(jwtToken)
                .expiresIn(300)
                .build();

            logger.info("Successfully generated QR questionnaire URL for user: {} - Hospital: {} - Questionnaire ID: {}", 
                user.getId(), hospitalCode, questionnaireId);
            logger.info("=== QR QUESTIONNAIRE URL GENERATION END ===");

            return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("QR 문진표 URL 생성 성공", response));
        } catch (Exception e) {
            logger.error("Error generating QR questionnaire URL - Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("QR 문진표 URL 생성 실패: " + e.getMessage(), null));
        }
    }

    // 커스텀 토큰(문진표 열람용) 전용 공개 API
    @GetMapping("/public/{id:\\d+}")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> getQuestionnaireByCustomToken(
            @PathVariable Integer id,
            @RequestParam("jwtToken") String jwtToken) {
        logger.info("[커스텀 토큰 전용] getQuestionnaireByCustomToken called. id: {}, jwtToken: {}", id, jwtToken != null ? jwtToken.substring(0, Math.min(jwtToken.length(), 20)) + "..." : null);
        boolean valid = tokenService.validateCustomJwtToken(jwtToken, id);
        logger.info("[커스텀 토큰 전용] tokenService.validateCustomJwtToken result: {}", valid);
        if (!valid) {
            logger.warn("[커스텀 토큰 전용] Invalid custom token for questionnaireId: {}", id);
            return ResponseEntity.status(401)
                .body(ApiResponse.error("유효하지 않은 커스텀 토큰입니다.", null));
        }
        PatientQuestionnaire questionnaire = patientQuestionnaireService.getQuestionnaireByIdPublic(id);
        PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire);
        logger.info("[커스텀 토큰 전용] Successfully retrieved questionnaire by custom token - Questionnaire ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("문진표 조회 성공 (커스텀 토큰)", response));
    }
} 