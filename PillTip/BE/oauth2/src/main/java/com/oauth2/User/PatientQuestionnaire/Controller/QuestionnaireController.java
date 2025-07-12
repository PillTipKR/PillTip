// author : mireutale
// description : 문진표 관련 API 컨트롤러
package com.oauth2.User.PatientQuestionnaire.Controller;

import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireSummaryResponse;
import com.oauth2.User.PatientQuestionnaire.Service.PatientQuestionnaireService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.UserInfo.Dto.UserPermissionsRequest;
import com.oauth2.User.UserInfo.Dto.UserPermissionsResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.UserInfo.Service.UserPermissionsService;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireRequest;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireResponse;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.User.PatientQuestionnaire.Dto.QuestionnaireAvailabilityResponse;
import com.oauth2.User.PatientQuestionnaire.Dto.QRQuestionnaireResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.User.Auth.Service.TokenService;
import com.oauth2.User.Hospital.HospitalService;
import com.oauth2.Util.Encryption.EncryptionUtil;

@RestController
@RequestMapping("/api/questionnaire")
@RequiredArgsConstructor
public class QuestionnaireController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireController.class);
    private final UserPermissionsService userPermissionsService;
    private final PatientQuestionnaireService patientQuestionnaireService;
    private final TokenService tokenService;
    private final HospitalService hospitalService;
    private final EncryptionUtil encryptionUtil;
    
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
    public ResponseEntity<ApiResponse<java.util.List<PatientQuestionnaireSummaryResponse>>> getUserQuestionnaireList(
            @AuthenticationPrincipal User user) {
        java.util.List<PatientQuestionnaireSummaryResponse> list = patientQuestionnaireService.getUserQuestionnaireSummaries(user);
        return ResponseEntity.status(200)
            .body(ApiResponse.success("문진표 리스트 조회 성공", list));
    }
    
    // 현재 접속한 유저의 문진표 조회
    @GetMapping("")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> getCurrentUserQuestionnaire(
            @AuthenticationPrincipal User user) {
        
        logger.info("Received getCurrentUserQuestionnaire request for user: {}", user.getId());
        
        try {
            PatientQuestionnaire questionnaire = patientQuestionnaireService.getCurrentUserQuestionnaire(user);
            PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire, null, user.getRealName(), user.getAddress(), encryptionUtil);
            logger.info("Successfully retrieved current user questionnaire for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("현재 유저 문진표 조회 성공", response));
        } catch (IllegalArgumentException e) {
            logger.warn("No questionnaire found for user: {} - Error: {}", user.getId(), e.getMessage());
            return ResponseEntity.status(404)
                .body(ApiResponse.error("작성된 문진표가 없습니다.", null));
        } catch (Exception e) {
            logger.error("Error retrieving current user questionnaire for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("문진표 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
    
    // 문진표 작성
    @PostMapping("")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> createQuestionnaire(
            @AuthenticationPrincipal User user,
            @RequestBody PatientQuestionnaireRequest request) {
        // Validate realName and address
        if (request.getRealName() == null || request.getRealName().trim().isEmpty() ||
            request.getAddress() == null || request.getAddress().trim().isEmpty() ||
            request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("실명과 주소, 전화번호는 필수입니다.", null));
        }
        
        // 권한 체크
        try {
            UserPermissionsResponse permissions = userPermissionsService.getUserPermissions(user);
            if (!permissions.isSensitiveInfoPermission() || !permissions.isMedicalInfoPermission()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("문진표 작성을 위한 권한이 없습니다. 민감정보 및 의료정보 동의가 필요합니다.", null));
            }
        } catch (Exception e) {
            logger.error("Error checking permissions for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("권한 확인 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
        
        logger.info("Received createQuestionnaire request for user: {}", user.getId());
        try {
            PatientQuestionnaire questionnaire = patientQuestionnaireService.createQuestionnaire(user, request);
            PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire, request.getPhoneNumber(), user.getRealName(), user.getAddress(), encryptionUtil);
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
            @PathVariable Long id,
            @RequestParam(value = "jwtToken", required = false) String jwtToken,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        logger.info("Received getQuestionnaireById request for user: {} - ID: {}", user.getId(), id);
        
        try {
            // JWT 토큰이 제공된 경우 커스텀 검증
            if (jwtToken != null && !jwtToken.trim().isEmpty()) {
                logger.info("Trying custom token validation for questionnaireId: {}, token: {}", id, jwtToken);
                boolean valid = tokenService.validateCustomJwtToken(jwtToken, id);
                logger.info("Custom token validation result for questionnaireId {}: {}", id, valid);
                
                if (valid) {
                    // 토큰이 유효하면 소유자 검증 없이 조회
                    PatientQuestionnaire questionnaire = patientQuestionnaireService.getQuestionnaireByIdPublic(id);
                    PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire, null, null, null, encryptionUtil);
                    logger.info("Successfully retrieved questionnaire by custom token for ID: {}", id);
                    return ResponseEntity.status(200)
                        .body(ApiResponse.success("문진표 조회 성공", response));
                } else {
                    logger.warn("Custom token validation failed for questionnaireId: {}", id);
                    return ResponseEntity.status(401)
                        .body(ApiResponse.error("유효하지 않은 토큰입니다.", null));
                }
            }
            
            // 일반적인 소유자 검증을 통한 조회
            PatientQuestionnaire questionnaire = patientQuestionnaireService.getQuestionnaireById(user, id);
            PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire, null, user.getRealName(), user.getAddress(), encryptionUtil);
            logger.info("Successfully retrieved questionnaire for user: {} - ID: {}", user.getId(), id);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("문진표 조회 성공", response));
                
        } catch (SecurityException e) {
            logger.warn("Security violation for questionnaire access - User: {} - ID: {} - Error: {}", user.getId(), id, e.getMessage());
            return ResponseEntity.status(403)
                .body(ApiResponse.error("본인 문진표만 조회할 수 있습니다.", null));
        } catch (IllegalArgumentException e) {
            logger.warn("Questionnaire not found - User: {} - ID: {} - Error: {}", user.getId(), id, e.getMessage());
            return ResponseEntity.status(404)
                .body(ApiResponse.error("문진표를 찾을 수 없습니다.", null));
        } catch (Exception e) {
            logger.error("Error retrieving questionnaire for user: {} - ID: {} - Error: {}", user.getId(), id, e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("문진표 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
    
    // 현재 유저의 문진표 삭제
    @DeleteMapping("")
    public ResponseEntity<ApiResponse<String>> deleteCurrentUserQuestionnaire(
            @AuthenticationPrincipal User user) {
        
        logger.info("Received deleteCurrentUserQuestionnaire request for user: {}", user.getId());
        
        try {
            patientQuestionnaireService.deleteCurrentUserQuestionnaire(user);
            logger.info("Successfully deleted current user questionnaire for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("문진표 삭제 성공", "문진표가 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            logger.warn("No questionnaire found for deletion - User: {} - Error: {}", user.getId(), e.getMessage());
            return ResponseEntity.status(404)
                .body(ApiResponse.error("작성된 문진표가 없습니다.", null));
        } catch (Exception e) {
            logger.error("Error deleting current user questionnaire for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("문진표 삭제 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
    
    // 현재 유저의 문진표 수정
    @PutMapping("")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> updateCurrentUserQuestionnaire(
            @AuthenticationPrincipal User user,
            @RequestBody PatientQuestionnaireRequest request) {
        
        logger.info("Received updateCurrentUserQuestionnaire request for user: {}", user.getId());
        
        try {
            PatientQuestionnaire questionnaire = patientQuestionnaireService.updateCurrentUserQuestionnaire(user, request);
            // 업데이트된 User 정보를 사용하여 응답 생성
            PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire, request.getPhoneNumber(), request.getRealName(), request.getAddress(), encryptionUtil);
            logger.info("Successfully updated current user questionnaire for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("문진표 수정 성공", response));
        } catch (IllegalArgumentException e) {
            // 약물 검증 실패인지 확인
            if (e.getMessage().contains("복용 중인 약 목록에 없습니다")) {
                logger.warn("Invalid medication for update - User: {} - Error: {}", user.getId(), e.getMessage());
                return ResponseEntity.status(400)
                    .body(ApiResponse.error(e.getMessage(), null));
            } else {
                logger.warn("No questionnaire found for update - User: {} - Error: {}", user.getId(), e.getMessage());
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("작성된 문진표가 없습니다.", null));
            }
        } catch (Exception e) {
            logger.error("Error updating current user questionnaire for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("문진표 수정 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
    // QR 코드를 통한 문진표 URL 생성 API (현재 유저의 문진표)
    @PostMapping("/qr-url/{hospitalCode}")
    public ResponseEntity<QRQuestionnaireResponse> generateQRUrl(
            @AuthenticationPrincipal User user,
            @PathVariable String hospitalCode) {
        
        logger.info("User ID: {}, Hospital Code: {}", user.getId(), hospitalCode);
        
        try {
            // 1. 병원 코드 유효성 검사
            if (!hospitalService.existsByHospitalCode(hospitalCode)) {
                logger.warn("Invalid hospital code: {}", hospitalCode);
                return ResponseEntity.notFound().build();
            }
            
            // 2. 현재 유저의 문진표 존재 여부 확인
            PatientQuestionnaire questionnaire = patientQuestionnaireService.getCurrentUserQuestionnaire(user);
            
            // 3. JWT 토큰 생성 (3분)
            String jwtToken = tokenService.createCustomJwtToken(
                user.getId(),
                questionnaire.getQuestionnaireId(),
                hospitalCode,
                180
            );
            
            // 4. QR URL 생성
            String qrUrl = String.format("http://localhost:3000/api/questionnaire/public/%s?token=%s",
                questionnaire.getQuestionnaireId(),
                jwtToken);
            
            QRQuestionnaireResponse response = QRQuestionnaireResponse.builder()
                .questionnaireId(questionnaire.getQuestionnaireId())
                .qrUrl(qrUrl)
                .patientName(user.getRealName() != null ? user.getRealName() : user.getNickname())
                .hospitalCode(hospitalCode)
                .accessToken(jwtToken)
                .expiresInMinutes(180) // 3분
                .build();
            
            logger.info("QR URL generated successfully for user: {} - Hospital Code: {}", 
                user.getId(), hospitalCode);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("No questionnaire found for QR generation - User: {} - Error: {}", user.getId(), e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error generating QR URL for user: {} - Hospital Code: {}", 
                user.getId(), hospitalCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 커스텀 토큰(문진표 열람용) 전용 공개 API
    @GetMapping("/public/{id:\\d+}")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> getQuestionnaireByCustomToken(
            @PathVariable Long id,
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
        logger.info("[커스텀 토큰 전용] questionnaire: {}", questionnaire);
        PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire, questionnaire.getUser().getUserProfile().getPhone(), questionnaire.getUser().getRealName(), questionnaire.getUser().getAddress(), encryptionUtil);
        logger.info("[커스텀 토큰 전용] Successfully retrieved questionnaire by custom token - Questionnaire ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success("문진표 조회 성공 (커스텀 토큰)", response));
    }
} 