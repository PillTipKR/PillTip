package com.oauth2.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PatientQuestionnaireSummaryResponse {
    private Integer questionnaireId;
    private String questionnaireName;
    private LocalDate issueDate;
    private LocalDate lastModifiedDate;
} 