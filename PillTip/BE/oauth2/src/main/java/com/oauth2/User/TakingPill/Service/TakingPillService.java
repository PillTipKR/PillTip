package com.oauth2.User.TakingPill.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.TakingPill.Dto.TakingPillRequest;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Dto.TakingPillDetailResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.TakingPill.Entity.DosageLog;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.TakingPill.Entity.DosageSchedule;
import com.oauth2.User.TakingPill.Repositoty.DosageLogRepository;
import com.oauth2.User.TakingPill.Repositoty.TakingPillRepository;
import com.oauth2.User.TakingPill.Repositoty.DosageScheduleRepository;
import com.oauth2.Util.Encryption.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TakingPillService {
    private static final Logger logger = LoggerFactory.getLogger(TakingPillService.class);
    private final TakingPillRepository takingPillRepository;
    private final DosageScheduleRepository dosageScheduleRepository;
    private final ObjectMapper objectMapper;
    private final DosageLogRepository dosageLogRepository;
    private final EncryptionUtil encryptionUtil;

    /**
     * 복용 중인 약을 추가합니다.
     */
    public TakingPill addTakingPill(User user, TakingPillRequest request) {
        // 요청 데이터 검증
        validateTakingPillRequest(request);
        
        // 기존에 같은 약품이 있는지 확인
        List<TakingPill> existingPills = takingPillRepository.findByUserAndMedicationId(user, request.getMedicationId());
        if (!existingPills.isEmpty()) {
            throw new RuntimeException("이미 복용 중인 약품입니다.");
        }
        
        // TakingPill 엔티티 생성
        TakingPill takingPill = TakingPill.builder()
                .user(user)
                .medicationId(request.getMedicationId())
                .medicationName(request.getMedicationName())
                .startYear(request.getStartDate() != null ? request.getStartDate().getYear() : null)
                .startMonth(request.getStartDate() != null ? request.getStartDate().getMonthValue() : null)
                .startDay(request.getStartDate() != null ? request.getStartDate().getDayOfMonth() : null)
                .endYear(request.getEndDate() != null ? request.getEndDate().getYear() : null)
                .endMonth(request.getEndDate() != null ? request.getEndDate().getMonthValue() : null)
                .endDay(request.getEndDate() != null ? request.getEndDate().getDayOfMonth() : null)
                .alarmName(request.getAlarmName())
                .daysOfWeek(convertDaysOfWeekToJson(request.getDaysOfWeek()))
                .dosageAmount(request.getDosageAmount())
                .build();
        
        TakingPill savedTakingPill = takingPillRepository.save(takingPill);
        
        // DosageSchedule 엔티티들 생성 및 저장
        if (request.getDosageSchedules() != null){
            for (TakingPillRequest.DosageSchedule scheduleRequest : request.getDosageSchedules()) {
                DosageSchedule dosageSchedule = DosageSchedule.builder()
                        .takingPill(savedTakingPill)
                        .hour(scheduleRequest.getHour())
                        .minute(scheduleRequest.getMinute())
                        .period(scheduleRequest.getPeriod())
                        .alarmOnOff(scheduleRequest.isAlarmOnOff())
                        .dosageUnit(scheduleRequest.getDosageUnit())
                        .build();

                dosageScheduleRepository.save(dosageSchedule);
            }
        }
        List<DosageSchedule> schedules = savedTakingPill.getDosageSchedules();

        if (schedules != null && !schedules.isEmpty() && request.getStartDate() != null && request.getEndDate() != null) {
            LocalDate date = request.getStartDate();

            while (!date.isAfter(request.getEndDate())) {
                if (matchesToday(savedTakingPill, date)) {
                    for (DosageSchedule schedule : schedules) {
                        DosageLog dosageLog = DosageLog.builder()
                                .scheduledTime(LocalDateTime.of(date,
                                        LocalTime.of(schedule.getHour(), schedule.getMinute())))
                                .user(user)
                                .alarmName(request.getAlarmName())
                                .medicationName(request.getMedicationName())
                                .build();
                        dosageLogRepository.save(dosageLog);
                    }
                }
                date = date.plusDays(1);
            }
        }

        return savedTakingPill;
    }

    /**
     * 복용 중인 약을 삭제합니다.
     */
    public void deleteTakingPill(User user, String medicationId) {
        Long medId = Long.parseLong(medicationId);
        List<TakingPill> takingPills = takingPillRepository.findByUserAndMedicationId(user, medId);
        
        if (takingPills.isEmpty()) {
            throw new RuntimeException("해당 약품을 찾을 수 없습니다.");
        }
        
        // TakingPill 삭제 시 연관된 DosageSchedule도 함께 삭제됨 (cascade 설정)
        takingPillRepository.deleteAll(takingPills);
    }

    /**
     * 복용 중인 약을 수정합니다.
     */
    public TakingPill updateTakingPill(User user, TakingPillRequest request) {
        // 요청 데이터 검증
        validateTakingPillRequest(request);
        
        // 기존 TakingPill 찾기
        List<TakingPill> existingPills = takingPillRepository.findByUserAndMedicationId(user, request.getMedicationId());
        if (existingPills.isEmpty()) {
            throw new RuntimeException("수정할 약품을 찾을 수 없습니다.");
        }
        
        TakingPill takingPill = existingPills.get(0);
        
        // TakingPill 정보 업데이트
        takingPill.setMedicationName(request.getMedicationName());
        takingPill.setStartYear(request.getStartDate() != null ? request.getStartDate().getYear() : null);
        takingPill.setStartMonth(request.getStartDate() != null ? request.getStartDate().getMonthValue() : null);
        takingPill.setStartDay(request.getStartDate() != null ? request.getStartDate().getDayOfMonth() : null);
        takingPill.setEndYear(request.getEndDate() != null ? request.getEndDate().getYear() : null);
        takingPill.setEndMonth(request.getEndDate() != null ? request.getEndDate().getMonthValue() : null);
        takingPill.setEndDay(request.getEndDate() != null ? request.getEndDate().getDayOfMonth() : null);
        takingPill.setAlarmName(request.getAlarmName());
        takingPill.setDaysOfWeek(convertDaysOfWeekToJson(request.getDaysOfWeek()));
        takingPill.setDosageAmount(request.getDosageAmount());
        
        // 기존 DosageSchedule 리스트를 클리어하고 새로운 스케줄로 교체
        takingPill.getDosageSchedules().clear();
        
        // 새로운 DosageSchedule 생성 및 추가
        if (request.getDosageSchedules() != null) {
            for (TakingPillRequest.DosageSchedule scheduleRequest : request.getDosageSchedules()) {
                DosageSchedule dosageSchedule = DosageSchedule.builder()
                        .takingPill(takingPill)
                        .hour(scheduleRequest.getHour())
                        .minute(scheduleRequest.getMinute())
                        .period(scheduleRequest.getPeriod())
                        .alarmOnOff(scheduleRequest.isAlarmOnOff())
                        .dosageUnit(scheduleRequest.getDosageUnit())
                        .build();
                
                takingPill.getDosageSchedules().add(dosageSchedule);
            }
        }
        
        // TakingPill과 연관된 DosageSchedule들을 함께 저장
        TakingPill updatedTakingPill = takingPillRepository.save(takingPill);

        return updatedTakingPill;
    }

    /**
     * 복용 중인 약 정보를 요약 형태로 반환합니다.
     */
    public TakingPillSummaryResponse getTakingPillSummary(User user) {
        List<TakingPill> takingPills = takingPillRepository.findByUser(user);
        
        List<TakingPillSummaryResponse.TakingPillSummary> summaries = takingPills.stream()
                .map(pill -> TakingPillSummaryResponse.TakingPillSummary.builder()
                        .medicationId(pill.getMedicationId())
                        .medicationName(getDecryptedMedicationName(pill))
                        .alarmName(getDecryptedAlarmName(pill))
                        .startDate(createSafeLocalDate(pill.getStartYear(), pill.getStartMonth(), pill.getStartDay()))
                        .endDate(createSafeLocalDate(pill.getEndYear(), pill.getEndMonth(), pill.getEndDay()))
                        .dosageAmount(pill.getDosageAmount())
                        .build())
                .collect(Collectors.toList());
        
        return TakingPillSummaryResponse.builder()
                .takingPills(summaries)
                .build();
    }

    /**
     * 복용 중인 약 정보를 상세 형태로 반환합니다.
     */
    public TakingPillDetailResponse getTakingPillDetail(User user) {
        List<TakingPill> takingPills = takingPillRepository.findByUserWithDosageSchedules(user);
        
        List<TakingPillDetailResponse.TakingPillDetail> details = takingPills.stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
        
        return TakingPillDetailResponse.builder()
                .takingPills(details)
                .build();
    }

    /**
     * 특정 약의 상세 정보를 조회합니다.
     */
    public TakingPillDetailResponse.TakingPillDetail getTakingPillDetailById(User user, Long medicationId) {
        List<TakingPill> takingPills = takingPillRepository.findByUserAndMedicationId(user, medicationId);
        
        if (takingPills.isEmpty()) {
            throw new RuntimeException("Medication not found with ID: " + medicationId);
        }
        
        TakingPill takingPill = takingPills.get(0);
        return convertToDetailResponse(takingPill);
    }

    /**
     * 특정 사용자의 모든 복용 중인 약을 조회합니다.
     */
    public List<TakingPill> getTakingPillsByUser(User user) {
        return takingPillRepository.findByUserWithDosageSchedules(user);
    }

    /**
     * TakingPill 엔티티를 DetailResponse로 변환합니다.
     */
    private TakingPillDetailResponse.TakingPillDetail convertToDetailResponse(TakingPill takingPill) {
        return TakingPillDetailResponse.TakingPillDetail.builder()
                .medicationId(takingPill.getMedicationId())
                .medicationName(getDecryptedMedicationName(takingPill))
                .startDate(createSafeLocalDate(takingPill.getStartYear(), takingPill.getStartMonth(), takingPill.getStartDay()))
                .endDate(createSafeLocalDate(takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay()))
                .alarmName(getDecryptedAlarmName(takingPill))
                .daysOfWeek(parseDaysOfWeekFromJson(getDecryptedDaysOfWeek(takingPill)))
                .dosageAmount(takingPill.getDosageAmount())
                .dosageSchedules(takingPill.getDosageSchedules().stream()
                        .map(schedule -> TakingPillDetailResponse.DosageScheduleDetail.builder()
                                .hour(schedule.getHour())
                                .minute(schedule.getMinute())
                                .period(schedule.getPeriod())
                                .dosageUnit(schedule.getDosageUnit())
                                .alarmOnOff(schedule.getAlarmOnOff())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 암호화된 약물명을 복호화합니다.
     */
    private String getDecryptedMedicationName(TakingPill takingPill) {
        try {
            String encryptedName = takingPill.getMedicationName();
            if (encryptedName != null && !encryptedName.isEmpty()) {
                return encryptionUtil.decrypt(encryptedName);
            }
        } catch (Exception e) {
            logger.warn("Failed to decrypt medication name for takingPill {}: {}", takingPill.getId(), e.getMessage());
        }
        return takingPill.getMedicationName(); // 복호화 실패 시 원본 반환
    }

    /**
     * 암호화된 알림명을 복호화합니다.
     */
    private String getDecryptedAlarmName(TakingPill takingPill) {
        try {
            String encryptedName = takingPill.getAlarmName();
            if (encryptedName != null && !encryptedName.isEmpty()) {
                return encryptionUtil.decrypt(encryptedName);
            }
        } catch (Exception e) {
            logger.warn("Failed to decrypt alarm name for takingPill {}: {}", takingPill.getId(), e.getMessage());
        }
        return takingPill.getAlarmName(); // 복호화 실패 시 원본 반환
    }

    /**
     * 암호화된 요일 정보를 복호화합니다.
     */
    private String getDecryptedDaysOfWeek(TakingPill takingPill) {
        try {
            String encryptedDays = takingPill.getDaysOfWeek();
            if (encryptedDays != null && !encryptedDays.isEmpty()) {
                return encryptionUtil.decrypt(encryptedDays);
            }
        } catch (Exception e) {
            logger.warn("Failed to decrypt days of week for takingPill {}: {}", takingPill.getId(), e.getMessage());
        }
        return takingPill.getDaysOfWeek(); // 복호화 실패 시 원본 반환
    }

    /**
     * 요일 리스트를 JSON 문자열로 변환합니다.
     */
    private String convertDaysOfWeekToJson(List<String> daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(daysOfWeek);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert days of week to JSON", e);
        }
    }

    /**
     * JSON 문자열을 요일 리스트로 변환합니다.
     */
    private List<String> parseDaysOfWeekFromJson(String daysOfWeekJson) {
        if (daysOfWeekJson == null || daysOfWeekJson.isEmpty()) {
            return List.of();
        }
        
        try {
            return objectMapper.readValue(daysOfWeekJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse days of week from JSON", e);
        }
    }

    /**
     * 안전한 LocalDate 생성 메서드
     */
    private LocalDate createSafeLocalDate(Integer year, Integer month, Integer day) {
        if (year == null || month == null || day == null) {
            return LocalDate.now(); // 기본값으로 오늘 날짜 반환
        }
        
        // 월이 0이거나 12보다 큰 경우 기본값으로 1월 사용
        int safeMonth = (month <= 0 || month > 12) ? 1 : month;
        
        // 일이 0이거나 31보다 큰 경우 기본값으로 1일 사용
        int safeDay = (day <= 0 || day > 31) ? 1 : day;
        
        try {
            return LocalDate.of(year, safeMonth, safeDay);
        } catch (Exception e) {
            // 날짜 생성 실패 시 오늘 날짜 반환
            return LocalDate.now();
        }
    }

    /**
     * TakingPill이 오늘 복용해야 하는지 확인
     */
    public boolean matchesToday(TakingPill takingPill, LocalDate today) {
        // 복용 기간 확인 - 년, 월, 일로 분리된 필드에서 LocalDate 생성
        LocalDate startDate = createSafeLocalDate(takingPill.getStartYear(), takingPill.getStartMonth(), takingPill.getStartDay());
        LocalDate endDate = createSafeLocalDate(takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay());

        if (startDate.isAfter(today) || endDate.isBefore(today)) {
            return false;
        }

        // 요일 확인
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> daysOfWeek = mapper.readValue(takingPill.getDaysOfWeek(),
                    new TypeReference<>() {
                    });

            // 매일 복용인 경우
            if (daysOfWeek.contains("EVERYDAY")) {
                return true;
            }

            // 특정 요일 복용인 경우
            String todayOfWeek = today.getDayOfWeek().name().substring(0, 3); // MON, TUE, WED, etc.
            return daysOfWeek.contains(todayOfWeek);

        } catch (JsonProcessingException e) {
            // JSON 파싱 실패 시 false 반환
            return false;
        }
    }

    public int to24Hour(Integer hour, String period) {
        if (hour == 12) hour = 0;
        return "PM".equalsIgnoreCase(period) ? hour + 12 : hour;
    }

    /**
     * 요청 데이터 검증 메서드
     */
    private void validateTakingPillRequest(TakingPillRequest request) {
        if (request.getMedicationId() == null) {
            throw new RuntimeException("약품 ID는 필수입니다.");
        }
        
        if (request.getMedicationName() == null || request.getMedicationName().trim().isEmpty()) {
            throw new RuntimeException("약품 이름은 필수입니다.");
        }
        
        // startDate와 endDate가 모두 null이거나 모두 설정되어야 함
        if ((request.getStartDate() == null) != (request.getEndDate() == null)) {
            throw new RuntimeException("시작일과 종료일은 모두 설정되거나 모두 null이어야 합니다.");
        }
        
        // startDate와 endDate가 모두 설정된 경우에만 날짜 검증
        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new RuntimeException("시작일은 종료일보다 이전이어야 합니다.");
            }
        }
        
        // alarmName은 null 허용
        
        // daysOfWeek는 null 허용
        
        // dosageSchedules는 null 허용 (빈 배열도 허용)
        
        // 복용 스케줄이 있는 경우에만 검증
        if (request.getDosageSchedules() != null && !request.getDosageSchedules().isEmpty()) {
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
} 