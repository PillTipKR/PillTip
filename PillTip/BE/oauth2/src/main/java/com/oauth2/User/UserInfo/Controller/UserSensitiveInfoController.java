// author : mireutale
// description : 사용자 민감정보 관리 컨트롤러
package com.oauth2.User.UserInfo.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDeleteRequest;
import com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDto;
import com.oauth2.User.UserInfo.Service.UserSensitiveInfoService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sensitive-info")
@RequiredArgsConstructor
public class UserSensitiveInfoController {

    private static final Logger logger = LoggerFactory.getLogger(UserSensitiveInfoController.class);
    private final UserSensitiveInfoService userSensitiveInfoService;

    /**
     * 사용자 민감정보 조회
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<UserSensitiveInfoDto>> getSensitiveInfo(
            @AuthenticationPrincipal User user) {
        logger.info("Received getSensitiveInfo request for user: {}", user.getId());
        
        try {
            UserSensitiveInfoDto sensitiveInfo = userSensitiveInfoService.getSensitiveInfo(user);
            if (sensitiveInfo == null) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("민감정보가 존재하지 않습니다.", null));
            }
            
            logger.info("Successfully retrieved sensitive info for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("민감정보 조회 성공", sensitiveInfo));
        } catch (Exception e) {
            logger.error("Error retrieving sensitive info for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("민감정보 조회 실패: " + e.getMessage(), null));
        }
    }

    /**
     * 사용자 민감정보 생성/업데이트
     */
    @PutMapping("")
    public ResponseEntity<ApiResponse<UserSensitiveInfoDto>> updateSensitiveInfo(
            @AuthenticationPrincipal User user,
            @RequestBody UserSensitiveInfoDto request) {
        logger.info("Received updateSensitiveInfo request for user: {}", user.getId());
        
        try {
            List<String> medicationInfo = request.getMedicationInfo();
            List<String> allergyInfo = request.getAllergyInfo();
            List<String> chronicDiseaseInfo = request.getChronicDiseaseInfo();
            List<String> surgeryHistoryInfo = request.getSurgeryHistoryInfo();
            
            UserSensitiveInfoDto sensitiveInfo = userSensitiveInfoService.saveOrUpdateSensitiveInfo(
                user, medicationInfo, allergyInfo, chronicDiseaseInfo, surgeryHistoryInfo);
            
            logger.info("Successfully updated sensitive info for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("민감정보 업데이트 성공", sensitiveInfo));
        } catch (Exception e) {
            logger.error("Error updating sensitive info for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("민감정보 업데이트 실패: " + e.getMessage(), null));
        }
    }

    /**
     * 특정 카테고리의 민감정보만 업데이트
     */
    @PutMapping("/{category}")
    public ResponseEntity<ApiResponse<UserSensitiveInfoDto>> updateSensitiveInfoCategory(
            @AuthenticationPrincipal User user,
            @PathVariable String category,
            @RequestBody List<String> data) {
        logger.info("Received updateSensitiveInfoCategory request for user: {} - Category: {}", user.getId(), category);
        
        try {
            UserSensitiveInfoDto sensitiveInfo = userSensitiveInfoService.updateSensitiveInfoCategory(user, category, data);
            
            logger.info("Successfully updated sensitive info category for user: {} - Category: {}", user.getId(), category);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("민감정보 카테고리 업데이트 성공", sensitiveInfo));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid category for user: {} - Category: {}", user.getId(), category);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("잘못된 카테고리입니다: " + e.getMessage(), null));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing sensitive info category for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("민감정보 카테고리 직렬화 실패: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error updating sensitive info category for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("민감정보 카테고리 업데이트 실패: " + e.getMessage(), null));
        }
    }

    /**
     * 사용자 민감정보 선택적 삭제 (boolean으로 지정된 카테고리만 유지)
     */
    @DeleteMapping("")
    public ResponseEntity<ApiResponse<UserSensitiveInfoDto>> deleteSensitiveInfoCategories(
            @AuthenticationPrincipal User user,
            @RequestBody UserSensitiveInfoDeleteRequest request) {
        logger.info("Received deleteSensitiveInfoCategories request for user: {}", user.getId());
        
        try {
            UserSensitiveInfoDto sensitiveInfo = userSensitiveInfoService.deleteSensitiveInfoCategories(user, request);
            
            if (sensitiveInfo == null) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("민감정보가 존재하지 않습니다.", null));
            }
            
            logger.info("Successfully deleted sensitive info categories for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("민감정보 카테고리 삭제 성공", sensitiveInfo));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing sensitive info for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("민감정보 직렬화 실패: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error deleting sensitive info categories for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("민감정보 카테고리 삭제 실패: " + e.getMessage(), null));
        }
    }

    /**
     * 사용자 민감정보 전체 삭제
     */
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse<String>> deleteAllSensitiveInfo(
            @AuthenticationPrincipal User user) {
        logger.info("Received deleteAllSensitiveInfo request for user: {}", user.getId());
        
        try {
            userSensitiveInfoService.deleteAllSensitiveInfoByUser(user);
            
            logger.info("Successfully deleted all sensitive info for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("민감정보 전체 삭제 성공", "민감정보가 성공적으로 삭제되었습니다."));
        } catch (Exception e) {
            logger.error("Error deleting all sensitive info for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("민감정보 전체 삭제 실패: " + e.getMessage(), null));
        }
    }
} 