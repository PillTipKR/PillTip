package com.oauth2.User.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.entity.DosageSchedule;
import com.oauth2.User.entity.TakingPill;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class TakingPillResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("medication_id")
    private Long medicationId;

    @JsonProperty("medication_name")
    private String medicationName;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("alert_name")
    private String alertName;

    @JsonProperty("days_of_week")
    private List<String> daysOfWeek;

    @JsonProperty("dosage_schedules")
    private List<DosageScheduleResponse> dosageSchedules;

    public TakingPillResponse(TakingPill takingPill) {
        this.id = takingPill.getId();
        this.medicationId = takingPill.getMedicationId();
        this.medicationName = takingPill.getMedicationName();
        this.startDate = takingPill.getStartDate();
        this.endDate = takingPill.getEndDate();
        this.alertName = takingPill.getAlarmName();
        
        // JSON 문자열을 리스트로 변환
        this.daysOfWeek = parseDaysOfWeek(takingPill.getDaysOfWeek());
        
        // dosageSchedules가 null일 경우 빈 리스트로 초기화
        if (takingPill.getDosageSchedules() != null) {
            this.dosageSchedules = takingPill.getDosageSchedules().stream()
                    .map(DosageScheduleResponse::new)
                    .collect(Collectors.toList());
        } else {
            this.dosageSchedules = new ArrayList<>();
        }
    }

    private List<String> parseDaysOfWeek(String daysOfWeekJson) {
        if (daysOfWeekJson == null || daysOfWeekJson.isEmpty()) {
            return List.of();
        }
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(daysOfWeekJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // 파싱 실패 시 빈 리스트 반환
            return List.of();
        }
    }

    @Getter
    @Setter
    public static class DosageScheduleResponse {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("hour")
        private Integer hour;

        @JsonProperty("minute")
        private Integer minute;

        @JsonProperty("period")
        private String period;

        @JsonProperty("dosage_amount")
        private Double dosageAmount;

        @JsonProperty("dosage_unit")
        private String dosageUnit;

        public DosageScheduleResponse(DosageSchedule dosageSchedule) {
            this.id = dosageSchedule.getId();
            this.hour = dosageSchedule.getHour();
            this.minute = dosageSchedule.getMinute();
            this.period = dosageSchedule.getPeriod();
            this.dosageUnit = dosageSchedule.getDosageUnit();
        }
    }
} 