package com.oauth2.User.PatientQuestionnaire.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRQuestionnaireResponse {
    private String questionnaireUrl;
    private String patientName;
    private boolean patientPregnant;
    private String hospitalCode;
    private Integer questionnaireId;
    private String accessToken;
    private int expiresInMinutes;
} 