package com.oauth2.User.TakingPill.Dto;


import java.util.List;

public record DosageLogResponse (
    int medTotal,
    int medTaken,
    String medicationName,
    List<DosageScheduleDto> dosageSchedule
){}

