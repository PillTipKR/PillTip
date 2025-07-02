package com.oauth2.User.service;

import com.oauth2.User.dto.TakingPillRequest;
import com.oauth2.User.dto.TakingPillSummaryResponse;
import com.oauth2.User.dto.TakingPillDetailResponse;
import com.oauth2.User.entity.User;
import com.oauth2.User.entity.UserProfile;
import com.oauth2.User.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;

    public UserProfile addTakingPill(User user, TakingPillRequest request) {
        // 요청 데이터 검증
        validateTakingPillRequest(request);
        
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));

        List<TakingPillRequest> takingPills = getTakingPillsList(userProfile);
        
        // 기존에 같은 약품이 있는지 확인
        boolean exists = takingPills.stream()
            .anyMatch(pill -> pill.getMedicationId() != null && 
                            request.getMedicationId() != null && 
                            pill.getMedicationId().equals(request.getMedicationId()));
        
        if (exists) {
            throw new RuntimeException("이미 복용 중인 약품입니다.");
        }
        
        takingPills.add(request);
        return saveTakingPills(userProfile, takingPills);
    }

    public UserProfile deleteTakingPill(User user, String medicationId) {
        Long medId = Long.parseLong(medicationId);
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));

        List<TakingPillRequest> takingPills = getTakingPillsList(userProfile);
        takingPills = takingPills.stream()
            .filter(pill -> !pill.getMedicationId().equals(medId))
            .collect(Collectors.toList());
        
        return saveTakingPills(userProfile, takingPills);
    }

    public UserProfile updateTakingPill(User user, TakingPillRequest request) {
        // 요청 데이터 검증
        validateTakingPillRequest(request);
        
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));

        List<TakingPillRequest> takingPills = getTakingPillsList(userProfile);
        takingPills = takingPills.stream()
            .map(pill -> (pill.getMedicationId() != null && 
                         request.getMedicationId() != null && 
                         pill.getMedicationId().equals(request.getMedicationId())) ? request : pill)
            .collect(Collectors.toList());
        
        return saveTakingPills(userProfile, takingPills);
    }

    public UserProfile getTakingPill(User user) {
        return userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));
    }

    /**
     * 복용 중인 약 정보를 요약 형태로 반환합니다.
     * @param user 현재 로그인한 사용자
     * @return 복용 중인 약 정보 요약
     */
    public TakingPillSummaryResponse getTakingPillSummary(User user) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));

        List<TakingPillRequest> takingPills = getTakingPillsList(userProfile);
        
        List<TakingPillSummaryResponse.TakingPillSummary> summaries = takingPills.stream()
            .map(pill -> TakingPillSummaryResponse.TakingPillSummary.builder()
                .medicationId(pill.getMedicationId())
                .medicationName(pill.getMedicationName())
                .alarmName(pill.getAlarmName())
                .startDate(pill.getStartDate())
                .endDate(pill.getEndDate())
                .dosageAmount(pill.getDosageAmount())
                .build())
            .collect(Collectors.toList());
        
        return TakingPillSummaryResponse.builder()
            .takingPills(summaries)
            .build();
    }

    /**
     * 복용 중인 약 정보를 상세 형태로 반환합니다.
     * @param user 현재 로그인한 사용자
     * @return 복용 중인 약 정보 상세
     */
    public TakingPillDetailResponse getTakingPillDetail(User user) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));

        List<TakingPillRequest> takingPills = getTakingPillsList(userProfile);
        
        List<TakingPillDetailResponse.TakingPillDetail> details = takingPills.stream()
            .map(pill -> TakingPillDetailResponse.TakingPillDetail.builder()
                .medicationId(pill.getMedicationId())
                .medicationName(pill.getMedicationName())
                .startDate(pill.getStartDate())
                .endDate(pill.getEndDate())
                .alarmName(pill.getAlarmName())
                .daysOfWeek(pill.getDaysOfWeek())
                .dosageAmount(pill.getDosageAmount())
                .dosageSchedules(pill.getDosageSchedules() == null ? new ArrayList<>() : pill.getDosageSchedules().stream()
                    .map(schedule -> TakingPillDetailResponse.DosageScheduleDetail.builder()
                        .hour(schedule.getHour())
                        .minute(schedule.getMinute())
                        .period(schedule.getPeriod())
                        .dosageUnit(schedule.getDosageUnit())
                        .alarmOnOff(schedule.isAlarmOnOff())
                        .build())
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList());
        
        return TakingPillDetailResponse.builder()
            .takingPills(details)
            .build();
    }

    /**
     * 특정 약의 상세 정보를 조회합니다.
     * @param user 현재 로그인한 사용자
     * @param medicationId 약품 ID
     * @return 특정 약의 상세 정보
     */
    public TakingPillDetailResponse.TakingPillDetail getTakingPillDetailById(User user, Long medicationId) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));

        List<TakingPillRequest> takingPills = getTakingPillsList(userProfile);
        
        TakingPillRequest targetPill = takingPills.stream()
            .filter(pill -> pill.getMedicationId().equals(medicationId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Medication not found with ID: " + medicationId));
        
        return TakingPillDetailResponse.TakingPillDetail.builder()
            .medicationId(targetPill.getMedicationId())
            .medicationName(targetPill.getMedicationName())
            .startDate(targetPill.getStartDate())
            .endDate(targetPill.getEndDate())
            .alarmName(targetPill.getAlarmName())
            .daysOfWeek(targetPill.getDaysOfWeek())
            .dosageAmount(targetPill.getDosageAmount())
            .dosageSchedules(targetPill.getDosageSchedules() == null ? new ArrayList<>() : targetPill.getDosageSchedules().stream()
                .map(schedule -> TakingPillDetailResponse.DosageScheduleDetail.builder()
                    .hour(schedule.getHour())
                    .minute(schedule.getMinute())
                    .period(schedule.getPeriod())
                    .dosageUnit(schedule.getDosageUnit())
                    .alarmOnOff(schedule.isAlarmOnOff())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    public UserProfile updatePregnant(User user, boolean pregnant) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfile.setPregnant(pregnant);
        return userProfileRepository.save(userProfile);
    }

    private List<TakingPillRequest> getTakingPillsList(UserProfile userProfile) {
        if (userProfile.getTakingPills() == null || userProfile.getTakingPills().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(userProfile.getTakingPills(), 
                new TypeReference<List<TakingPillRequest>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse taking pills", e);
        }
    }

    private UserProfile saveTakingPills(UserProfile userProfile, List<TakingPillRequest> takingPills) {
        try {
            String takingPillsJson = objectMapper.writeValueAsString(takingPills);
            userProfile.setTakingPills(takingPillsJson);
            return userProfileRepository.save(userProfile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save taking pills", e);
        }
    }

    // 요청 데이터 검증 메서드
    private void validateTakingPillRequest(TakingPillRequest request) {
        if (request.getMedicationId() == null) {
            throw new RuntimeException("약품 ID는 필수입니다.");
        }
        
        if (request.getMedicationName() == null || request.getMedicationName().trim().isEmpty()) {
            throw new RuntimeException("약품 이름은 필수입니다.");
        }
        
        if (request.getStartDate() == null) {
            throw new RuntimeException("시작일은 필수입니다.");
        }
        
        if (request.getEndDate() == null) {
            throw new RuntimeException("종료일은 필수입니다.");
        }
        
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("시작일은 종료일보다 이전이어야 합니다.");
        }
        
        if (request.getAlarmName() == null || request.getAlarmName().trim().isEmpty()) {
            throw new RuntimeException("알림명은 필수입니다.");
        }
        
        if (!request.isValidDaysOfWeek()) {
            throw new RuntimeException("유효하지 않은 요일 정보입니다.");
        }
        
        if (request.getDosageSchedules() == null || request.getDosageSchedules().isEmpty()) {
            throw new RuntimeException("복용 스케줄은 최소 1개 이상 필요합니다.");
        }
        
        // 복용 스케줄 검증
        for (TakingPillRequest.DosageSchedule schedule : request.getDosageSchedules()) {
            if (!schedule.isValidHour()) {
                throw new RuntimeException("시간은 0-12 사이의 값이어야 합니다.");
            }
            if (!schedule.isValidMinute()) {
                throw new RuntimeException("분은 0-59 사이의 값이어야 합니다.");
            }
            if (!schedule.isValidPeriod()) {
                throw new RuntimeException("기간은 AM 또는 PM이어야 합니다.");
            }
        }
    }
}

