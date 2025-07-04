package com.oauth2.User.TakingPill.Controller;

import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.TakingPill.Dto.TakingPillRequest;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Dto.TakingPillDetailResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/taking-pill")
@RequiredArgsConstructor
public class TakingPillController {

    private static final Logger logger = LoggerFactory.getLogger(TakingPillController.class);
    private final TakingPillService takingPillService;

    // 복용 중인 약 추가
    @PostMapping
    public ResponseEntity<ApiResponse<TakingPillDetailResponse>> addTakingPill(
            @AuthenticationPrincipal User user,
            @RequestBody TakingPillRequest request) {
        logger.info("Received addTakingPill request for user: {}", user.getId());
        logger.debug("TakingPillRequest details: {}", request);
        
        try {
            takingPillService.addTakingPill(user, request);
            TakingPillDetailResponse takingPillDetail = takingPillService.getTakingPillDetail(user);
            logger.info("Successfully added taking pill for user: {}", user.getId());
            return ResponseEntity.status(201)
                .body(ApiResponse.success("Taking pill added successfully", takingPillDetail));
        } catch (Exception e) {
            logger.error("Error adding taking pill for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to add taking pill: " + e.getMessage(), null));
        }
    }

    // 복용 중인 약 삭제
    @DeleteMapping("/{medicationId}")
    public ResponseEntity<ApiResponse<TakingPillSummaryResponse>> deleteTakingPill(
            @AuthenticationPrincipal User user,
            @PathVariable String medicationId) {
        logger.info("Received deleteTakingPill request for user: {}", user.getId());
        logger.debug("Medication ID to delete: {}", medicationId);
        
        try {
            takingPillService.deleteTakingPill(user, medicationId);
            TakingPillSummaryResponse takingPillSummary = takingPillService.getTakingPillSummary(user);
            logger.info("Successfully deleted taking pill for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Taking pill deleted successfully", takingPillSummary));
        } catch (Exception e) {
            logger.error("Error deleting taking pill for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to delete taking pill: " + e.getMessage(), null));
        }
    }

    // 복용 중인 약 수정
    @PutMapping("/{medicationId}")
    public ResponseEntity<ApiResponse<TakingPillDetailResponse>> updateTakingPill(
            @AuthenticationPrincipal User user,
            @PathVariable String medicationId,
            @RequestBody TakingPillRequest request) {
        logger.info("Received updateTakingPill request for user: {} - Medication ID: {}", user.getId(), medicationId);
        logger.debug("TakingPillRequest details: {}", request);
        
        try {
            // URL 경로의 medicationId를 request에 설정
            request.setMedicationId(Long.parseLong(medicationId));
            
            takingPillService.updateTakingPill(user, request);
            TakingPillDetailResponse takingPillDetail = takingPillService.getTakingPillDetail(user);
            logger.info("Successfully updated taking pill for user: {} - Medication ID: {}", user.getId(), medicationId);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Taking pill updated successfully", takingPillDetail));
        } catch (NumberFormatException e) {
            logger.error("Invalid medication ID format for user: {} - Medication ID: {}", user.getId(), medicationId);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Invalid medication ID format", null));
        } catch (Exception e) {
            logger.error("Error updating taking pill for user: {} - Medication ID: {} - Error: {}", user.getId(), medicationId, e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to update taking pill: " + e.getMessage(), null));
        }
    }

    // 복용 중인 약 조회 (요약)
    @GetMapping
    public ResponseEntity<ApiResponse<TakingPillSummaryResponse>> getTakingPill(
            @AuthenticationPrincipal User user) {
        logger.info("Received getTakingPill request for user: {}", user.getId());
        
        try {
            TakingPillSummaryResponse takingPillSummary = takingPillService.getTakingPillSummary(user);
            logger.info("Successfully retrieved taking pill summary for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Taking pill summary retrieved successfully", takingPillSummary));
        } catch (Exception e) {
            logger.error("Error retrieving taking pill summary for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to retrieve taking pill summary: " + e.getMessage(), null));
        }
    }

    // 복용 중인 약 상세 조회
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<TakingPillDetailResponse>> getTakingPillDetail(
            @AuthenticationPrincipal User user) {
        logger.info("Received getTakingPillDetail request for user: {}", user.getId());
        
        try {
            TakingPillDetailResponse takingPillDetail = takingPillService.getTakingPillDetail(user);
            logger.info("Successfully retrieved taking pill detail for user: {}", user.getId());
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Taking pill detail retrieved successfully", takingPillDetail));
        } catch (Exception e) {
            logger.error("Error retrieving taking pill detail for user: {} - Error: {}", user.getId(), e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to retrieve taking pill detail: " + e.getMessage(), null));
        }
    }

    // 특정 약의 상세 정보 조회
    @GetMapping("/{medicationId}")
    public ResponseEntity<ApiResponse<TakingPillDetailResponse.TakingPillDetail>> getTakingPillDetailById(
            @AuthenticationPrincipal User user,
            @PathVariable String medicationId) {
        logger.info("Received getTakingPillDetailById request for user: {} - Medication ID: {}", user.getId(), medicationId);
        
        try {
            TakingPillDetailResponse.TakingPillDetail pillDetail = takingPillService.getTakingPillDetailById(user, Long.parseLong(medicationId));
            logger.info("Successfully retrieved taking pill detail for user: {} - Medication ID: {}", user.getId(), medicationId);
            return ResponseEntity.status(200)
                .body(ApiResponse.success("Taking pill detail retrieved successfully", pillDetail));
        } catch (NumberFormatException e) {
            logger.error("Invalid medication ID format for user: {} - Medication ID: {}", user.getId(), medicationId);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Invalid medication ID format", null));
        } catch (Exception e) {
            logger.error("Error retrieving taking pill detail for user: {} - Medication ID: {} - Error: {}", user.getId(), medicationId, e.getMessage(), e);
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Failed to retrieve taking pill detail: " + e.getMessage(), null));
        }
    }
} 