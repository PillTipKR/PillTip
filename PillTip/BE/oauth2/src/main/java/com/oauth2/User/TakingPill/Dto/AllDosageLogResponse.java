package com.oauth2.User.TakingPill.Dto;

import java.util.List;

public record AllDosageLogResponse(
        int total,
        int taken,
        List<DosageLogResponse> perDrugLogs
) {}
