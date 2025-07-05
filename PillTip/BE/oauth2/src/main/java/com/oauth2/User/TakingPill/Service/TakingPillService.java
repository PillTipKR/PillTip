package com.oauth2.User.TakingPill.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.TakingPill.Dto.TakingPillRequest;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Dto.TakingPillDetailResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.TakingPill.Entity.DosageLog;
import com.oauth2.User.TakingPill.Entity.PillStatus;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.TakingPill.Entity.DosageSchedule;
import com.oauth2.User.TakingPill.Repositoty.DosageLogRepository;
import com.oauth2.User.TakingPill.Repositoty.TakingPillRepository;
import com.oauth2.User.TakingPill.Repositoty.DosageScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.oauth2.User.TakingPill.Entity.PillStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TakingPillService {
    private final TakingPillRepository takingPillRepository;
    private final DosageScheduleRepository dosageScheduleRepository;
    private final ObjectMapper objectMapper;
    private final DosageLogRepository dosageLogRepository;

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
                .startYear(request.getStartDate().getYear())
                .startMonth(request.getStartDate().getMonthValue())
                .startDay(request.getStartDate().getDayOfMonth())
                .endYear(request.getEndDate().getYear())
                .endMonth(request.getEndDate().getMonthValue())
                .endDay(request.getEndDate().getDayOfMonth())
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

                // 양방향 연관관계 유지
                savedTakingPill.getDosageSchedules().add(dosageSchedule);
                dosageScheduleRepository.save(dosageSchedule);
            }
        }
        List<DosageSchedule> schedules = savedTakingPill.getDosageSchedules();
        if (!schedules.isEmpty()) {
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

        TakingPill takingPill = takingPills.get(0);

        LocalDateTime now = LocalDateTime.now();

        // 복약 기간 계산
        LocalDate endDate = createSafeLocalDate(
                takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay());

        // 복약 상태 판단
        PillStatus status = PillStatus.calculateStatus(endDate, takingPill.getCreatedAt());

        if (status == COMPLETED) {
            throw new IllegalStateException("이미 종료된 복약 기록은 수정할 수 없습니다.");
        }

        // 연관된 DosageLog 삭제 정책에 따라 처리
        List<DosageLog> logs = dosageLogRepository.findByUserAndMedicationName(user, takingPill.getMedicationName());

        if (status == NEW) {
            // 전체 삭제
            dosageLogRepository.deleteAll(logs);
        } else if (status == ACTIVE) {
            List<DosageLog> futureLogs = logs.stream()
                    .filter(log -> !log.getScheduledTime().isBefore(now)) // now 포함 이후
                    .collect(Collectors.toList());

            dosageLogRepository.deleteAll(futureLogs);
        }

        // TakingPill 삭제 (cascade로 DosageSchedule도 삭제됨)
        takingPillRepository.delete(takingPill);
    }


    /**
     * 복용 중인 약을 수정합니다.
     */

    //수정을 누른 시점이

    public TakingPill updateTakingPill(User user, TakingPillRequest request) {
        // 요청 데이터 검증
        validateTakingPillRequest(request);
        
        // 기존 TakingPill 찾기
        List<TakingPill> existingPills = takingPillRepository.findByUserAndMedicationId(user, request.getMedicationId());
        if (existingPills.isEmpty()) {
            throw new RuntimeException("수정할 약품을 찾을 수 없습니다.");
        }
        
        TakingPill takingPill = existingPills.get(0);

        // === 복약 로그 동기화 ===
        LocalDate oldStartDate = createSafeLocalDate(takingPill.getStartYear(), takingPill.getStartMonth(), takingPill.getStartDay());
        LocalDate oldEndDate = createSafeLocalDate(takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay());

        // 기존 로그 조회
        List<DosageLog> existingLogs = dosageLogRepository.findByUserAndMedicationName(user, takingPill.getMedicationName());
        List<DosageSchedule> dosageSchedules = takingPill.getDosageSchedules();
        // TakingPill 정보 업데이트
        takingPill.setMedicationName(request.getMedicationName());
        takingPill.setStartYear(request.getStartDate().getYear());
        takingPill.setStartMonth(request.getStartDate().getMonthValue());
        takingPill.setStartDay(request.getStartDate().getDayOfMonth());
        takingPill.setEndYear(request.getEndDate().getYear());
        takingPill.setEndMonth(request.getEndDate().getMonthValue());
        takingPill.setEndDay(request.getEndDate().getDayOfMonth());
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

        LocalDate newStartDate = request.getStartDate();
        LocalDate newEndDate = request.getEndDate();

        // 복약 상태 판단
        PillStatus pillStatus = PillStatus.calculateStatus(oldEndDate,takingPill.getCreatedAt());
        takingPill.setCreatedAt(takingPill.getCreatedAt() != null ? takingPill.getCreatedAt() : LocalDateTime.now());

        // 날짜 및 스케줄 변화 감지
        boolean isStartDateEarlier = newStartDate.isBefore(oldStartDate);
        boolean isStartDatePushed = newStartDate.isAfter(oldStartDate);
        boolean isEndDateExtended = newEndDate.isAfter(oldEndDate);
        boolean isEndDateShortened = newEndDate.isBefore(oldEndDate);
        boolean isScheduleChanged = !isScheduleEqual(takingPill.getDosageSchedules(), request.getDosageSchedules());
        System.out.println(isStartDateEarlier + " " + isStartDatePushed +" "+ isEndDateExtended + " " + isEndDateShortened+ " "+isScheduleChanged);
        // 상태 기반 처리
        handleDosageLogsOnUpdate(
                user,
                takingPill,
                existingLogs,
                pillStatus,
                oldStartDate,
                oldEndDate,
                isStartDateEarlier,
                isStartDatePushed,
                isEndDateExtended,
                isEndDateShortened,
                isScheduleChanged
        );


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
                        .medicationName(pill.getMedicationName())
                        .alarmName(pill.getAlarmName())
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
                .medicationName(takingPill.getMedicationName())
                .startDate(createSafeLocalDate(takingPill.getStartYear(), takingPill.getStartMonth(), takingPill.getStartDay()))
                .endDate(createSafeLocalDate(takingPill.getEndYear(), takingPill.getEndMonth(), takingPill.getEndDay()))
                .alarmName(takingPill.getAlarmName())
                .daysOfWeek(parseDaysOfWeekFromJson(takingPill.getDaysOfWeek()))
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
     * 요일 리스트를 JSON 문자열로 변환합니다.
     */
    private String convertDaysOfWeekToJson(List<String> daysOfWeek) {
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
        //System.out.println(startDate + " " + endDate + " "+ today);
        //System.out.println("daysOfWeek raw: " + takingPill.getDaysOfWeek());

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
            System.out.println(todayOfWeek);
            return daysOfWeek.contains(todayOfWeek);

        } catch (JsonProcessingException e) {
            // JSON 파싱 실패 시 false 반환
            System.out.println("파싱실패");
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

    private void handleDosageLogsOnUpdate(
            User user,
            TakingPill pill,
            List<DosageLog> existingLogs,
            PillStatus status,
            LocalDate oldStart,
            LocalDate oldEnd,
            boolean isStartDateEarlier,
            boolean isStartDatePushed,
            boolean isEndDateExtended,
            boolean isEndDateShortened,
            boolean isScheduleChanged
    ) {
        LocalDateTime now = LocalDateTime.now();
        String medicationName = pill.getMedicationName();
        List<DosageSchedule> schedules = pill.getDosageSchedules();

        // 1. COMPLETED 상태: 아무것도 하지 않음
        if (status == COMPLETED) {
            return;
        }

        // 2. 시작일 앞당김 → 과거 로그 생성
        if (isStartDateEarlier) {
            LocalDate from = pillStartDate(pill).minusDays(1);
            List<DosageLog> backfill = generateDosageLogsBetween(user, medicationName, pill.getAlarmName(), from, oldStart, schedules, pill);
            System.out.println("Generated log count (backfill): " + backfill.size());
            dosageLogRepository.saveAll(backfill);
        }

        // 3. 시작일 뒤로 미룸 → 과거 로그 삭제 (NEW 상태일 때만)
        if (isStartDatePushed && status == PillStatus.NEW) {
            dosageLogRepository.deleteAll(
                    existingLogs.stream()
                            .filter(log -> log.getScheduledTime().toLocalDate().isBefore(pillStartDate(pill).minusDays(1)))
                            .collect(Collectors.toList())
            );
        }

        // 4. 종료일 연장 → 미래 로그 생성
        if (isEndDateExtended) {
            List<DosageLog> futureLogs = generateDosageLogsBetween(user, medicationName, pill.getAlarmName(), oldEnd, pillEndDate(pill).plusDays(1), schedules, pill);
            dosageLogRepository.saveAll(futureLogs);
        }

        // 5. 종료일 단축 → 미래 로그 삭제
        if (isEndDateShortened) {
            dosageLogRepository.deleteAll(
                    existingLogs.stream()
                            .filter(log -> log.getScheduledTime().toLocalDate().isAfter(pillEndDate(pill).minusDays(1)))
                            .collect(Collectors.toList())
            );
        }

        // 6. 스케줄 변경 시 → 현재~미래 로그 재생성 (ACTIVE는 과거 로그 보존)
        if (isScheduleChanged) {
            if (status == PillStatus.NEW) {
                // 모든 기존 로그 삭제 후 재생성
                dosageLogRepository.deleteAll(existingLogs);
                List<DosageLog> regenerated = generateDosageLogsBetween(
                        user, medicationName, pill.getAlarmName(),
                        pillStartDate(pill).minusDays(1), pillEndDate(pill).plusDays(1), schedules, pill
                );
                dosageLogRepository.saveAll(regenerated);
            } else if (status == PillStatus.ACTIVE) {
                // 미래 로그 중 isTaken=false 만 삭제
                List<DosageLog> futureLogsToRemove = existingLogs.stream()
                        .filter(log -> !log.isTaken() && !log.getScheduledTime().isBefore(now))
                        .collect(Collectors.toList());
                dosageLogRepository.deleteAll(futureLogsToRemove);

                // 미래 로그만 재생성
                List<DosageLog> regenerated = generateDosageLogsBetween(
                        user, medicationName, pill.getAlarmName(),
                        now.toLocalDate(), pillEndDate(pill).plusDays(1), schedules, pill
                );
                dosageLogRepository.saveAll(regenerated);
            }
        }
    }

    private List<DosageLog> generateDosageLogsBetween(
            User user,
            String medicationName,
            String alarmName,
            LocalDate from,
            LocalDate to,
            List<DosageSchedule> schedules,
            TakingPill pill
    ) {
        List<DosageLog> logs = new ArrayList<>();
        System.out.println(from + " " + to);
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            System.out.println("검사 중인 날짜: " + date + " / 요일: " + date.getDayOfWeek());
            if (!matchesToday(pill, date)) {
                System.out.println("SKIPPED date: " + date + " (not matching daysOfWeek)");
                continue;
            }

            for (DosageSchedule schedule : schedules) {
                int hour = to24Hour(schedule.getHour(), schedule.getPeriod());
                LocalTime time = LocalTime.of(hour, schedule.getMinute());
                LocalDateTime scheduledTime = LocalDateTime.of(date, time);

                DosageLog log = DosageLog.builder()
                        .user(user)
                        .medicationName(medicationName)
                        .alarmName(alarmName)
                        .scheduledTime(scheduledTime)
                        .build();

                logs.add(log);
            }
        }

        return logs;
    }

    private LocalDate pillStartDate(TakingPill pill) {
        return createSafeLocalDate(pill.getStartYear(), pill.getStartMonth(), pill.getStartDay());
    }

    private LocalDate pillEndDate(TakingPill pill) {
        return createSafeLocalDate(pill.getEndYear(), pill.getEndMonth(), pill.getEndDay());
    }

    private boolean isScheduleEqual(List<DosageSchedule> existing, List<TakingPillRequest.DosageSchedule> updated) {
        if (existing.size() != updated.size()) return false;

        for (int i = 0; i < existing.size(); i++) {
            DosageSchedule e = existing.get(i);
            TakingPillRequest.DosageSchedule u = updated.get(i);

            if (!e.getHour().equals(u.getHour())) return false;
            if (!e.getMinute().equals(u.getMinute())) return false;
            if (!e.getPeriod().equalsIgnoreCase(u.getPeriod())) return false;
            if (!e.getDosageUnit().equalsIgnoreCase(u.getDosageUnit())) return false;
        }
        return true;
    }

} 