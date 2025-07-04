package com.oauth2.User.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientQuestionnaireRequest {
    private String questionnaireName;

    private List<InfoItem> medicationInfo;
    private List<InfoItem> allergyInfo;
    private List<InfoItem> chronicDiseaseInfo;
    private List<InfoItem> surgeryHistoryInfo;

    private String notes;

    // Add these fields for user info update
    private String realName;
    private String address;
    private String phoneNumber;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InfoItem {
        private String id;
        private String medicationId;
        private String phoneNumber;
        private String allergyName;
        private String chronicDiseaseName;
        private String surgeryHistoryName;
        private boolean submitted;
    }
} 