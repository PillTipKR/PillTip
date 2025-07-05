package com.oauth2.User.Alarm.Init;

import com.oauth2.User.Alarm.Service.AlarmService;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.TakingPill.Entity.DosageLog;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.TakingPill.Repositoty.DosageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DrugAlarmScheduler {

    private final AlarmService alarmService;
    private final UserRepository userRepository;
    private final DosageLogRepository dosageLogRepository;

    @Scheduled(cron = "0 * * * * *")
    public void dispatchMedicationAlarms() {
        LocalDateTime now = LocalDateTime.now();

        List<User> users = userRepository.findAllActiveUsersWithPillInfo();

        for (User user : users) {
            // TakingPill 엔티티에서 직접 복용 중인 약 정보 조회
            List<DosageLog> dosageLogs = dosageLogRepository.findByUserAndDate(user.getId(),LocalDate.now());

            for(DosageLog dosageLog : dosageLogs) {
                LocalDateTime dateTime = dosageLog.getScheduledTime();
                if(dateTime.getHour() == now.getHour() && dateTime.getMinute() == now.getMinute()) {
                    alarmService.sendMedicationAlarm(user.getFCMToken(), dosageLog.getId(),
                            dosageLog.getAlarmName(), dosageLog.getMedicationName());
                }

                if(dosageLog.getIsRescheduled()){
                    LocalDateTime rescheduledTime = dosageLog.getRescheduledTime();
                    if(rescheduledTime.getHour() == now.getHour() && rescheduledTime.getMinute() == now.getMinute()) {
                        alarmService.sendMedicationAlarm(user.getFCMToken(), dosageLog.getId(),
                                dosageLog.getAlarmName(), dosageLog.getMedicationName());
                    }
                }
            }
        }
    }
}
