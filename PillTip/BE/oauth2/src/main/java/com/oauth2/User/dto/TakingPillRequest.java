package com.oauth2.User.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class TakingPillRequest {
    @JsonProperty("medication_id")
    private Long medicationId;
    
    @JsonProperty("medication_name")
    private String medicationName;
    
    @JsonProperty("dosage_amount")
    private Double dosageAmount;
    
    @JsonProperty("dosage_unit")
    private String dosageUnit;
    
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    
    @JsonProperty("interval_hours")
    private Integer intervalHours;
    
    @JsonProperty("duration_days")
    private Integer durationDays;
    
    @JsonProperty("times_per_day")
    private Integer timesPerDay;
    
    @JsonProperty("with_meal")
    private Boolean withMeal;
    
    private String notes;
} 