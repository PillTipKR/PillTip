// author : mireutale
// description : 문진표 관련 API 컨트롤러
package com.oauth2.User.PatientQuestionnaire.Controller;

import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireSummaryResponse;
import com.oauth2.User.PatientQuestionnaire.Service.PatientQuestionnaireService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.PatientQuestionnaire.Dto.QuestionnaireMessageConstants;
import com.oauth2.User.UserInfo.Dto.UserPermissionsRequest;
import com.oauth2.User.UserInfo.Dto.UserPermissionsResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.UserInfo.Service.UserPermissionsService;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireRequest;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireResponse;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.User.PatientQuestionnaire.Dto.QuestionnaireAvailabilityResponse;
import com.oauth2.User.PatientQuestionnaire.Dto.QRQuestionnaireResponse;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientPublicQuestionnaireResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.oauth2.User.Auth.Service.TokenService;
import com.oauth2.User.Hospital.HospitalService;
import com.oauth2.User.TakingPill.Service.TakingPillService;
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
    private final TakingPillService takingPillService;
    private final EncryptionUtil encryptionUtil;
    
    //동의사항 조회
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<UserPermissionsResponse>> getUserPermissions(
            @AuthenticationPrincipal User user) {
        try {
            UserPermissionsResponse permissions = userPermissionsService.getUserPermissions(user);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(QuestionnaireMessageConstants.PERMISSIONS_RETRIEVE_SUCCESS, permissions));
        } catch (Exception e) {
            logger.error("Permissions retrieve failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(QuestionnaireMessageConstants.PERMISSIONS_RETRIEVE_FAILED, null));
        }
    }
  
    //동의사항 수정
    @PutMapping("/permissions/multi")
    public ResponseEntity<ApiResponse<UserPermissionsResponse>> updateMedicalPermissions(
            @AuthenticationPrincipal User user,
            @RequestBody UserPermissionsRequest request) {
        try {
            UserPermissionsResponse permissions = userPermissionsService.updateMedicalPermissions(user, request);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(QuestionnaireMessageConstants.MEDICAL_PERMISSIONS_UPDATE_SUCCESS, permissions));
        } catch (Exception e) {
            logger.error("Medical permissions update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(QuestionnaireMessageConstants.PERMISSION_UPDATE_FAILED, null));
        }
    }
    //동의사항 수정 (여러개 수정)
    @PutMapping("/permissions/{permissionType}")
    public ResponseEntity<ApiResponse<UserPermissionsResponse>> updatePermission(
            @AuthenticationPrincipal User user,
            @PathVariable String permissionType,
            @RequestParam boolean granted) {
        try {
            UserPermissionsResponse permissions = userPermissionsService.updatePermission(user, permissionType, granted);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(QuestionnaireMessageConstants.PERMISSION_UPDATE_SUCCESS, permissions));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid permission type: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(QuestionnaireMessageConstants.INVALID_PERMISSION_TYPE, null));
        } catch (Exception e) {
            logger.error("Permission update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(QuestionnaireMessageConstants.PERMISSION_UPDATE_FAILED, null));
        }
    }
    //문진표 기능 사용 가능 여부 확인
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<QuestionnaireAvailabilityResponse>> isQuestionnaireAvailable(
            @AuthenticationPrincipal User user) {
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
                missingItems.add(QuestionnaireMessageConstants.MISSING_PERMISSIONS);
            }
            if (!personalInfoValid) {
                missingItems.add(QuestionnaireMessageConstants.MISSING_PERSONAL_INFO);
            }
            
            // 5. 메시지 생성
            String message;
            if (isAvailable) {
                message = QuestionnaireMessageConstants.QUESTIONNAIRE_AVAILABLE_MESSAGE;
            } else {
                message = QuestionnaireMessageConstants.QUESTIONNAIRE_UNAVAILABLE_MESSAGE + String.join(", ", missingItems);
            }
            
            QuestionnaireAvailabilityResponse response = QuestionnaireAvailabilityResponse.builder()
                .available(isAvailable)
                .permissionsValid(permissionsValid)
                .personalInfoValid(personalInfoValid)
                .missingItems(missingItems)
                .message(message)
                .build();
            
            return ResponseEntity.status(200)
                .body(ApiResponse.success(QuestionnaireMessageConstants.QUESTIONNAIRE_AVAILABILITY_CHECK_SUCCESS, response));
        } catch (Exception e) {
            logger.error("Questionnaire availability check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(QuestionnaireMessageConstants.QUESTIONNAIRE_RETRIEVE_FAILED, null));
        }
    }

    // ---------------------------------------문진표---------------------------------------
    // 문진표 리스트 조회 (구체적인 경로를 먼저 정의)
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<java.util.List<PatientQuestionnaireSummaryResponse>>> getUserQuestionnaireList(
            @AuthenticationPrincipal User user) {
        java.util.List<PatientQuestionnaireSummaryResponse> list = patientQuestionnaireService.getUserQuestionnaireSummaries(user);
        return ResponseEntity.status(200)
            .body(ApiResponse.success(QuestionnaireMessageConstants.QUESTIONNAIRE_LIST_RETRIEVE_SUCCESS, list));
    }
    
    // 현재 접속한 유저의 문진표 조회
    @GetMapping("")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> getCurrentUserQuestionnaire(
            @AuthenticationPrincipal User user) {
        try {
            PatientQuestionnaire questionnaire = patientQuestionnaireService.getCurrentUserQuestionnaire(user);
            Long expirationDate = System.currentTimeMillis() + 180 * 1000L;
            String phoneNumber = user.getUserProfile() != null ? user.getUserProfile().getPhone() : null;
            // 실시간 taking-pill 정보를 포함한 문진표 응답 생성
            PatientQuestionnaireResponse response = PatientQuestionnaireResponse.fromWithRealTimeMedication(
                questionnaire, phoneNumber, user.getRealName(), user.getAddress(), 
                encryptionUtil, takingPillService, expirationDate);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(QuestionnaireMessageConstants.CURRENT_USER_QUESTIONNAIRE_RETRIEVE_SUCCESS, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error(QuestionnaireMessageConstants.QUESTIONNAIRE_NOT_FOUND, null));
        } catch (Exception e) {
            logger.error("Current user questionnaire retrieve failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(QuestionnaireMessageConstants.QUESTIONNAIRE_RETRIEVE_FAILED, null));
        }
    }
    
    // // 문진표 작성
    // @PostMapping("")
    // public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> createQuestionnaire(
    //         @AuthenticationPrincipal User user,
    //         @RequestBody PatientQuestionnaireRequest request) {
    //     // Validate realName and address
    //     if (request.getRealName() == null || request.getRealName().trim().isEmpty() ||
    //         request.getAddress() == null || request.getAddress().trim().isEmpty() ||
    //         request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
    //         return ResponseEntity.status(400)
    //             .body(ApiResponse.error("실명과 주소, 전화번호는 필수입니다.", null));
    //     }
        
    //     // 권한 체크
    //     try {
    //         UserPermissionsResponse permissions = userPermissionsService.getUserPermissions(user);
    //         if (!permissions.isSensitiveInfoPermission() || !permissions.isMedicalInfoPermission()) {
    //             return ResponseEntity.status(403)
    //                 .body(ApiResponse.error("문진표 작성을 위한 권한이 없습니다. 민감정보 및 의료정보 동의가 필요합니다.", null));
    //         }
    //     } catch (Exception e) {
    //         logger.error("Error checking permissions for user: {} - Error: {}", user.getId(), e.getMessage(), e);
    //         return ResponseEntity.status(400)
    //             .body(ApiResponse.error("권한 확인 중 오류가 발생했습니다: " + e.getMessage(), null));
    //     }
        
    //     logger.info("Received createQuestionnaire request for user: {}", user.getId());
    //     try {
    //         PatientQuestionnaire questionnaire = patientQuestionnaireService.createQuestionnaire(user, request);
    //         Long expirationDate = System.currentTimeMillis() + 180 * 1000L;
    //         PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire, request.getPhoneNumber(), user.getRealName(), user.getAddress(), encryptionUtil, expirationDate);
    //         logger.info("Successfully created questionnaire for user: {}", user.getId());
    //         return ResponseEntity.status(201)
    //             .body(ApiResponse.success("Questionnaire created successfully", response));
    //     } catch (JsonProcessingException e) {
    //         logger.error("Error serializing questionnaire info for user: {} - Error: {}", user.getId(), e.getMessage(), e);
    //         return ResponseEntity.status(400)
    //             .body(ApiResponse.error("Failed to serialize questionnaire info: " + e.getMessage(), null));
    //     } catch (Exception e) {
    //         logger.error("Error creating questionnaire for user: {} - Error: {}", user.getId(), e.getMessage(), e);
    //         return ResponseEntity.status(400)
    //             .body(ApiResponse.error("Failed to create questionnaire: " + e.getMessage(), null));
    //     }
    // }
    
    // // 문진표 상세 조회 (숫자 ID만 허용, 커스텀 토큰도 허용)
    // @GetMapping("/{id:\\d+}")
    // public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> getQuestionnaireById(
    //         @AuthenticationPrincipal User user,
    //         @PathVariable Long id,
    //         @RequestParam(value = "jwtToken", required = false) String jwtToken,
    //         @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
    //     logger.info("Received getQuestionnaireById request for user: {} - ID: {}", user.getId(), id);
        
    //     try {
    //         // JWT 토큰이 제공된 경우 커스텀 검증
    //         if (jwtToken != null && !jwtToken.trim().isEmpty()) {
    //             logger.info("Trying custom token validation for questionnaireId: {}, token: {}", id, jwtToken);
    //             boolean valid = tokenService.validateCustomJwtToken(jwtToken, id);
    //             logger.info("Custom token validation result for questionnaireId {}: {}", id, valid);
                
    //             if (valid) {
    //                 // 토큰이 유효하면 소유자 검증 없이 조회
    //                 PatientQuestionnaire questionnaire = patientQuestionnaireService.getQuestionnaireByIdPublic(id);
    //                 Long expirationDate = System.currentTimeMillis() + 180 * 1000L;
    //                 PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire, null, null, null, encryptionUtil, expirationDate);
    //                 logger.info("Successfully retrieved questionnaire by custom token for ID: {}", id);
    //                 return ResponseEntity.status(200)
    //                     .body(ApiResponse.success("문진표 조회 성공", response));
    //             } else {
    //                 logger.warn("Custom token validation failed for questionnaireId: {}", id);
    //                 return ResponseEntity.status(401)
    //                     .body(ApiResponse.error("유효하지 않은 토큰입니다.", null));
    //             }
    //         }
            
    //         // 일반적인 소유자 검증을 통한 조회
    //         PatientQuestionnaire questionnaire = patientQuestionnaireService.getQuestionnaireById(user, id);
    //         Long expirationDate = System.currentTimeMillis() + 180 * 1000L;
    //         PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire, null, user.getRealName(), user.getAddress(), encryptionUtil, expirationDate);
    //         logger.info("Successfully retrieved questionnaire for user: {} - ID: {}", user.getId(), id);
    //         return ResponseEntity.status(200)
    //             .body(ApiResponse.success("문진표 조회 성공", response));
                
    //     } catch (SecurityException e) {
    //         logger.warn("Security violation for questionnaire access - User: {} - ID: {} - Error: {}", user.getId(), id, e.getMessage());
    //         return ResponseEntity.status(403)
    //             .body(ApiResponse.error("본인 문진표만 조회할 수 있습니다.", null));
    //     } catch (IllegalArgumentException e) {
    //         logger.warn("Questionnaire not found - User: {} - ID: {} - Error: {}", user.getId(), id, e.getMessage());
    //         return ResponseEntity.status(404)
    //             .body(ApiResponse.error("문진표를 찾을 수 없습니다.", null));
    //     } catch (Exception e) {
    //         logger.error("Error retrieving questionnaire for user: {} - ID: {} - Error: {}", user.getId(), id, e.getMessage(), e);
    //         return ResponseEntity.status(400)
    //             .body(ApiResponse.error("문진표 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
    //     }
    // }
    
    // 현재 유저의 문진표 삭제
    @DeleteMapping("")
    public ResponseEntity<ApiResponse<String>> deleteCurrentUserQuestionnaire(
            @AuthenticationPrincipal User user) {
        try {
            patientQuestionnaireService.deleteCurrentUserQuestionnaire(user);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(QuestionnaireMessageConstants.QUESTIONNAIRE_DELETE_SUCCESS, "문진표가 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error(QuestionnaireMessageConstants.QUESTIONNAIRE_NOT_FOUND, null));
        } catch (Exception e) {
            logger.error("Questionnaire delete failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(QuestionnaireMessageConstants.QUESTIONNAIRE_DELETE_FAILED, null));
        }
    }
    
    // 현재 유저의 문진표 수정
    @PutMapping("")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> updateCurrentUserQuestionnaire(
            @AuthenticationPrincipal User user,
            @RequestBody PatientQuestionnaireRequest request) {
        try {
            PatientQuestionnaire questionnaire = patientQuestionnaireService.updateCurrentUserQuestionnaire(user, request);
            Long expirationDate = System.currentTimeMillis() + 180 * 1000L;
            // 실시간 taking-pill 정보를 포함한 문진표 응답 생성
            PatientQuestionnaireResponse response = PatientQuestionnaireResponse.fromWithRealTimeMedication(
                questionnaire, request.getPhoneNumber(), request.getRealName(), request.getAddress(), 
                encryptionUtil, takingPillService, expirationDate);
            return ResponseEntity.status(200)
                .body(ApiResponse.success(QuestionnaireMessageConstants.QUESTIONNAIRE_UPDATE_SUCCESS, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error(QuestionnaireMessageConstants.QUESTIONNAIRE_NOT_FOUND, null));
        } catch (Exception e) {
            logger.error("Questionnaire update failed: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error(QuestionnaireMessageConstants.QUESTIONNAIRE_UPDATE_FAILED, null));
        }
    }
    // QR 코드를 통한 문진표 URL 생성 API (현재 유저의 문진표)
    @PostMapping("/qr-url/{hospitalCode}")
    public ResponseEntity<QRQuestionnaireResponse> generateQRUrl(
            @AuthenticationPrincipal User user,
            @PathVariable String hospitalCode) {
        try {
            // 1. 병원 코드 유효성 검사
            if (!hospitalService.existsByHospitalCode(hospitalCode)) {
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
            String qrUrl = String.format("http://localhost:3000/questionnaire/public/%s?jwtToken=%s",
                questionnaire.getQuestionnaireId(),
                jwtToken);
            
            QRQuestionnaireResponse response = QRQuestionnaireResponse.builder()
                .questionnaireId(questionnaire.getQuestionnaireId())
                .qrUrl(qrUrl)
                .patientName(user.getRealName() != null ? user.getRealName() : user.getNickname())
                .hospitalCode(hospitalCode)
                .hospitalName(hospitalService.findByHospitalCode(hospitalCode).getName())
                .accessToken(jwtToken)
                .expiresInMinutes(180) // 3분
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("QR URL generation failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 커스텀 토큰(문진표 열람용) 전용 공개 API
    @GetMapping("/public/{id:\\d+}")
    public ResponseEntity<ApiResponse<PatientPublicQuestionnaireResponse>> getQuestionnaireByCustomToken(
            @PathVariable Long id,
            @RequestParam("jwtToken") String jwtToken) {
        boolean valid = tokenService.validateCustomJwtToken(jwtToken, id);
        if (!valid) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error(QuestionnaireMessageConstants.INVALID_CUSTOM_TOKEN, null));
        }
        PatientQuestionnaire questionnaire = patientQuestionnaireService.getQuestionnaireByIdPublic(id);
        Long expirationDate = System.currentTimeMillis() + 180 * 1000L;
        // 실시간 taking-pill 정보를 포함한 공개 문진표 응답 생성
        PatientPublicQuestionnaireResponse response = PatientPublicQuestionnaireResponse.fromWithRealTimeMedication(
            questionnaire, questionnaire.getUser().getUserProfile().getPhone(), 
            questionnaire.getUser().getRealName(), questionnaire.getUser().getAddress(), 
            encryptionUtil, takingPillService, expirationDate);
        return ResponseEntity.ok(ApiResponse.success(QuestionnaireMessageConstants.PUBLIC_QUESTIONNAIRE_RETRIEVE_SUCCESS, response));
    }
} 