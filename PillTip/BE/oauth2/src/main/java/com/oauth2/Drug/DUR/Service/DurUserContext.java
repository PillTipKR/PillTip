package com.oauth2.Drug.DUR.Service;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class DurUserContext {
    private final boolean isElderly;
    private final boolean isPregnant;
    private final Map<String, List<Long>> classToDrugIdsMap;
    private final Set<String> userInteractionDrugNames;

    public DurUserContext(boolean isElderly, boolean isPregnant, Map<String, List<Long>> classToDrugIdsMap, Set<String> userInteractionDrugNames) {
        this.isElderly = isElderly;
        this.isPregnant = isPregnant;
        this.classToDrugIdsMap = classToDrugIdsMap;
        this.userInteractionDrugNames = userInteractionDrugNames;
    }
}
