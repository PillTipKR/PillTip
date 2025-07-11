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
    private String patientPhone;
    private String patientGender;
    private boolean patientPregnant;
    private String patientBirthDate;
    private String hospitalCode;
    private Integer questionnaireId;
    private String accessToken;
    private long expiresIn;  // 만료 시간 (초)
} 