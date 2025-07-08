package com.oauth2.User.TakingPill.Service;

import com.oauth2.User.TakingPill.Dto.AllDosageLogResponse;
import com.oauth2.User.TakingPill.Dto.DosageLogResponse;
import com.oauth2.User.TakingPill.Dto.DosageScheduleDto;
import com.oauth2.User.TakingPill.Entity.DosageLog;
import com.oauth2.User.TakingPill.Repositoty.DosageLogRepository;
import com.oauth2.User.TakingPill.Repositoty.DosageScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DosageLogService {

    private final DosageLogRepository dosageLogRepository;
    private final DosageScheduleRepository dosageScheduleRepository;

    public void updateTaken(Long dosageLogId) {
        // 복약 완료 상태 업데이트
        DosageLog dosageLog = dosageLogRepository.findById(dosageLogId)
                .orElseThrow(() -> new IllegalArgumentException("복약 기록을 찾을 수 없습니다"));

        if(!dosageLog.getIsTaken()){
            dosageLog.setTakenAt(LocalDateTime.now());  // 복약 완료 시간
            dosageLog.setIsTaken(true);  // 복약 완료 상태
        }else{
            dosageLog.setTakenAt(null);  // 복약 완료 시간
            dosageLog.setIsTaken(false);  // 복약 완료 상태
        }
        dosageLogRepository.save(dosageLog);
    }

    public void markPending(Long dosageLogId) {
        DosageLog dosageLog = dosageLogRepository.findById(dosageLogId)
                .orElseThrow(() -> new IllegalArgumentException("복약 기록을 찾을 수 없습니다."));
        dosageLog.setRescheduledTime(LocalDateTime.now().plusMinutes(5));
        dosageLogRepository.save(dosageLog);
    }

    public AllDosageLogResponse getDateLog(Long userId, LocalDate date) {
        List<DosageLog> logs = dosageLogRepository.findByUserAndDate(userId, date);
        Map<String, List<DosageLog>> grouped = logs.stream()
                .collect(Collectors.groupingBy(DosageLog::getMedicationName));

        return convertToDosageLogResponses(grouped);
    }

    private AllDosageLogResponse convertToDosageLogResponses(Map<String, List<DosageLog>> groupedLogs) {
        List<DosageLogResponse> responses = new ArrayList<>();
        int total = 0;
        int takenCount = 0;
        int totalPercent = 0;
        List<DosageLog> allLogs = groupedLogs.values().stream()
                .flatMap(List::stream)
                .toList();
        for (Map.Entry<String, List<DosageLog>> entry : groupedLogs.entrySet()) {
            String name = entry.getKey();
            List<DosageLog> logs = entry.getValue();

            List<DosageScheduleDto> scheduleDtos = logs.stream()
                    .map(log -> new DosageScheduleDto(
                            log.getId(),
                            log.getScheduledTime(),
                            log.getIsTaken(),
                            log.getTakenAt(),
                            log.getVisible()
                    ))
                    .collect(Collectors.toList());

            // 복약 완료 비율 계산
            int drugTotal = scheduleDtos.size();
            int drugTakenCount = Math.toIntExact(scheduleDtos.stream().filter(DosageScheduleDto::isTaken).count());
            int drugPercent = (int) (drugTotal == 0 ? 0.0 : (drugTakenCount * 100.0) / drugTotal);
            DosageLogResponse response = new DosageLogResponse(
                    drugPercent,
                    name,
                    scheduleDtos
            );
            responses.add(response);
        }
        total = allLogs.size();
        takenCount = Math.toIntExact(allLogs.stream().filter(DosageLog::getIsTaken).count());
        totalPercent = (int) (total == 0 ? 0.0 : (takenCount * 100.0) / total);

        return new AllDosageLogResponse(
                totalPercent,
                responses
        );
    }

}
