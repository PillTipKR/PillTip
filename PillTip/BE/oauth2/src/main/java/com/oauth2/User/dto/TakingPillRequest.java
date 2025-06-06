package com.oauth2.User.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class TakingPillRequest {
    private String medicationId;
    private String medicationName;
    private Double dosageAmount;
    private String dosageUnit;
    private LocalDateTime startTime;
    private Integer intervalHours;
    private Integer durationDays;
    private Integer timesPerDay;
    private Boolean withMeal;
    private String notes;
} 