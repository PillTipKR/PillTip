package com.oauth2.User.PatientQuestionnaire.Dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientQuestionnaireResponse {
    private Integer questionnaireId;
    private String questionnaireName;
    private String realName;
    private String address;
    private String phoneNumber;
    private LocalDate issueDate;
    private LocalDate lastModifiedDate;
    private String notes;
    private List<Map<String, Object>> medicationInfo;
    private List<Map<String, Object>> allergyInfo;
    private List<Map<String, Object>> chronicDiseaseInfo;
    private List<Map<String, Object>> surgeryHistoryInfo;

    public static PatientQuestionnaireResponse from(PatientQuestionnaire questionnaire) {
        ObjectMapper objectMapper = new ObjectMapper();
        
        try {
            return PatientQuestionnaireResponse.builder()
                    .questionnaireId(questionnaire.getQuestionnaireId())
                    .questionnaireName(questionnaire.getQuestionnaireName())
                    .realName(questionnaire.getUser().getRealName())
                    .address(questionnaire.getUser().getAddress())
                    .phoneNumber(questionnaire.getUser().getUserProfile() != null ? questionnaire.getUser().getUserProfile().getPhone() : null)
                    .issueDate(questionnaire.getIssueDate())
                    .lastModifiedDate(questionnaire.getLastModifiedDate())
                    .notes(questionnaire.getNotes())
                    .medicationInfo(parseJsonToList(questionnaire.getMedicationInfo(), objectMapper))
                    .allergyInfo(parseJsonToList(questionnaire.getAllergyInfo(), objectMapper))
                    .chronicDiseaseInfo(parseJsonToList(questionnaire.getChronicDiseaseInfo(), objectMapper))
                    .surgeryHistoryInfo(parseJsonToList(questionnaire.getSurgeryHistoryInfo(), objectMapper))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse questionnaire data", e);
        }
    }
    
    private static List<Map<String, Object>> parseJsonToList(String json, ObjectMapper objectMapper) throws JsonProcessingException {
        if (json == null || json.trim().isEmpty()) {
            return List.of();
        }
        return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
    }
} 