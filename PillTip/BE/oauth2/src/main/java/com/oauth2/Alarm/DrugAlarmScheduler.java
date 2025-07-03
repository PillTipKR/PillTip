package com.oauth2.Alarm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.dto.TakingPillRequest;
import com.oauth2.User.entity.User;
import com.oauth2.User.entity.TakingPill;
import com.oauth2.User.entity.DosageSchedule;
import com.oauth2.User.repository.UserRepository;
import com.oauth2.User.service.TakingPillService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DrugAlarmScheduler {

    private final AlarmService alarmService;
    private final UserRepository userRepository;
    private final TakingPillService takingPillService;

    @Scheduled(cron = "0 * * * * *") // 매 분마다 실행
    public void dispatchMedicationAlarms() throws JsonProcessingException {
        LocalTime now = LocalTime.now();

        List<User> users = userRepository.findAllActiveUsersWithPillInfo();

        for (User user : users) {
            // TakingPill 엔티티에서 직접 복용 중인 약 정보 조회
            List<TakingPill> takingPills = takingPillService.getTakingPillsByUser(user);

            for (TakingPill takingPill : takingPills) {
                // 오늘 복용해야 하는지 확인
                if (!matchesToday(takingPill, LocalDate.now())) continue;

                for (DosageSchedule dosageSchedule : takingPill.getDosageSchedules()) {
                    int targetHour = to24Hour(dosageSchedule.getHour(), dosageSchedule.getPeriod());
                    if (targetHour == now.getHour() && dosageSchedule.getMinute() == now.getMinute()) {
                        // 알림 전송
                        alarmService.sendMedicationAlarm(user.getFCMToken(), takingPill.getAlarmName(), takingPill.getMedicationName() + " 복약할 시간이에요!");
                    }
                }
            }
        }
    }

    /**
     * TakingPill이 오늘 복용해야 하는지 확인
     */
    private boolean matchesToday(TakingPill takingPill, LocalDate today) {
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
                    new TypeReference<List<String>>() {});
            
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
        if (hour == 12) hour = 0; // 12 AM = 0시
        return "PM".equalsIgnoreCase(period) ? hour + 12 : hour;
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
}
