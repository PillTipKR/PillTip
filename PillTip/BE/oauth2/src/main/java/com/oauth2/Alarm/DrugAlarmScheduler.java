package com.oauth2.Alarm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.dto.TakingPillRequest;
import com.oauth2.User.entity.User;
import com.oauth2.User.repository.UserRepository;
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

    @Scheduled(cron = "0 * * * * *") // 매 분마다 실행
    public void dispatchMedicationAlarms() throws JsonProcessingException {
        LocalTime now = LocalTime.now();
        ObjectMapper mapper = new ObjectMapper();

        List<User> users = userRepository.findAllWithMedicationInfo();

        for (User user : users) {
            String json = user.getUserProfile().getTakingPills(); // 복약 정보가 저장된 JSON 문자열

            List<TakingPillRequest> pills = mapper.readValue(json, new TypeReference<>() {});

            for (TakingPillRequest pill : pills) {
                // 호출부
                if (!pill.matchesToday(LocalDate.now())) continue;

                for (TakingPillRequest.DosageSchedule ds : pill.getDosageSchedules()) {
                    int targetHour = to24Hour(ds.getHour(), ds.getPeriod());
                    if (targetHour == now.getHour() && ds.getMinute() == now.getMinute()) {
                        // 알림 전송
                        alarmService.sendMedicationAlarm(user.getFCMToken(), pill.getAlarmName(), pill.getMedicationName() + " 복약할 시간이에요!");
                    }
                }
            }
        }
    }


    public int to24Hour(Integer hour, String period) {
        if (hour == 12) hour = 0; // 12 AM = 0시
        return "PM".equalsIgnoreCase(period) ? hour + 12 : hour;
    }


}
