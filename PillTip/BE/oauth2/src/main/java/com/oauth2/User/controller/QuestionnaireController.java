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
import com.oauth2.User.dto.QuestionnaireAvailabilityResponse;
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
    
    // 문진표 상세 조회 (숫자 ID만 허용)
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<PatientQuestionnaireResponse>> getQuestionnaireById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id) {
        logger.info("Received getQuestionnaireById request for user: {} - Questionnaire ID: {}", user.getId(), id);
        
        try {
            PatientQuestionnaire questionnaire = patientQuestionnaireService.getQuestionnaireById(user, id);
            PatientQuestionnaireResponse response = PatientQuestionnaireResponse.from(questionnaire);
            logger.info("Successfully retrieved questionnaire for user: {} - Questionnaire ID: {}", user.getId(), id);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("문진표 조회 성공", response));
        } catch (IllegalArgumentException e) {
            logger.error("Questionnaire not found for user: {} - Questionnaire ID: {} - Error: {}", user.getId(), id, e.getMessage());
            return ResponseEntity.status(404)
                .body(ApiResponse.error("문진표를 찾을 수 없습니다: " + e.getMessage(), null));
        } catch (SecurityException e) {
            logger.error("Access denied for user: {} - Questionnaire ID: {} - Error: {}", user.getId(), id, e.getMessage());
            return ResponseEntity.status(403)
                .body(ApiResponse.error("접근 권한이 없습니다: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error retrieving questionnaire for user: {} - Questionnaire ID: {} - Error: {}", user.getId(), id, e.getMessage(), e);
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
    // 외부 문진표 URL 발급 API
    @PostMapping("/external-url")
    public ResponseEntity<ApiResponse<String>> generateExternalQuestionnaireUrl(
            @AuthenticationPrincipal User user,
            @RequestBody ExternalQuestionnaireUrlRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        try {
            // 1. JWT 토큰에서 사용자 인증 (Spring Security에서 이미 인증됨)
            // 2. 문진표 id가 실제로 사용자의 문진표인지 검증
            patientQuestionnaireService.getQuestionnaireById(user, request.getQuestionnaireId());

            // 2-1. 병원 코드가 유효한지 확인
            if (!hospitalService.existsByHospitalCode(request.getHospitalCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("존재하지 않는 병원 코드입니다.", null));
            }

            // 3. 90초 유효한 JWT 토큰 생성 (payload: userId, questionnaireId, hospitalCode)
            String jwtToken = tokenService.createCustomJwtToken(
                user.getId(),
                request.getQuestionnaireId(),
                request.getHospitalCode(),
                90 // 90초
            );

            // 4. URL 생성
            String url = String.format("http://localhost:3000/questionnaire/%d?jwtToken=%s",
                    request.getQuestionnaireId(), jwtToken);
            return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("외부 문진표 URL 생성 성공", url));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("접근 권한이 없습니다: " + e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("문진표를 찾을 수 없습니다: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("외부 문진표 URL 생성 실패: " + e.getMessage(), null));
        }
    }

    // 외부 문진표 URL 요청 DTO
    public static class ExternalQuestionnaireUrlRequest {
        private Integer questionnaireId;
        private String hospitalCode;
        public Integer getQuestionnaireId() { return questionnaireId; }
        public void setQuestionnaireId(Integer questionnaireId) { this.questionnaireId = questionnaireId; }
        public String getHospitalCode() { return hospitalCode; }
        public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }
    }
} 