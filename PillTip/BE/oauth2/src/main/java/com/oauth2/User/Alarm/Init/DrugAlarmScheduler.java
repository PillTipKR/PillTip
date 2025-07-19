package com.oauth2.User.Alarm.Init;

import com.oauth2.User.Alarm.Dto.AlarmDto;
import com.oauth2.User.Alarm.Service.AlarmService;
import com.oauth2.User.TakingPill.Entity.DosageLog;
import com.oauth2.User.UserInfo.Repository.UserRepository;
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

        List<AlarmDto> alarmDtos = userRepository.findAllActiveUsersWithPillInfo();

        for (AlarmDto alarmDto : alarmDtos) {
            // TakingPill 엔티티에서 직접 복용 중인 약 정보 조회
            List<DosageLog> dosageLogs = dosageLogRepository.findByUserAndDate(alarmDto.userId(), LocalDate.now());
            List<DosageLog> rescheduled = dosageLogRepository.findByUserAndRescheduledDate(alarmDto.userId(), LocalDate.now());
            for(DosageLog dosageLog : dosageLogs) {
                LocalDateTime dateTime = dosageLog.getScheduledTime();
                if(dateTime.getHour() == now.getHour() && dateTime.getMinute() == now.getMinute()) {
                    alarmService.sendMedicationAlarm(alarmDto.FCMToken(), dosageLog.getId(),
                            dosageLog.getAlarmName(), dosageLog.getMedicationName());
                }
            }
            for(DosageLog dosageLog : rescheduled) {
                LocalDateTime rescheduledTime = dosageLog.getRescheduledTime();
                if(rescheduledTime.getHour() == now.getHour() && rescheduledTime.getMinute() == now.getMinute()) {
                    alarmService.sendMedicationAlarm(alarmDto.FCMToken(), dosageLog.getId(),
                            dosageLog.getAlarmName(), dosageLog.getMedicationName());
                }
            }
        }
    }
}
