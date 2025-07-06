package com.oauth2.Drug.DUR.Dto;


import java.util.List;
import java.util.Map;
import java.util.Set;

public record DurUserContext(
        boolean isElderly,
        boolean isPregnant,
        Map<String, List<Long>> classToDrugIdsMap,
        Set<String> userInteractionDrugNames
) {}
