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
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 * * * * *")
    public void dispatchMedicationAlarms() throws JsonProcessingException {
        LocalTime now = LocalTime.now();

        List<User> users = userRepository.findAllActiveUsersWithPillInfo();

        for (User user : users) {
            String json = user.getUserProfile().getTakingPills();
            if (json == null || json.isBlank()) {
                System.out.println("복약 JSON 없음 - userId: " + user.getId());
                continue; // JSON이 없으면 skip
            }else
                System.out.println("복약 JSON 존재! - userId: " + user.getId());

            List<TakingPillRequest> pills = objectMapper.readValue(json, new TypeReference<>() {});

            for (TakingPillRequest pill : pills) {
                if (!pill.matchesToday(LocalDate.now())) continue;

                for (TakingPillRequest.DosageSchedule ds : pill.getDosageSchedules()) {
                    int targetHour = to24Hour(ds.getHour(), ds.getPeriod());
                    if (targetHour == now.getHour() && ds.getMinute() == now.getMinute()) {
                        alarmService.sendMedicationAlarm(user.getFCMToken(), pill.getAlarmName(), pill.getMedicationName());
                    }
                }
            }
        }
    }

    public int to24Hour(Integer hour, String period) {
        if (hour == 12) hour = 0;
        return "PM".equalsIgnoreCase(period) ? hour + 12 : hour;
    }
}