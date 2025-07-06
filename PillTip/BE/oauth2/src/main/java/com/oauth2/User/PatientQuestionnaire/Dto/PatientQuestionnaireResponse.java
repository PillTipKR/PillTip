package com.oauth2.User.PatientQuestionnaire.Dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.Util.Encryption.EncryptionUtil;
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

    public static PatientQuestionnaireResponse from(PatientQuestionnaire questionnaire, String decryptedPhoneNumber, String decryptedRealName, String decryptedAddress, EncryptionUtil encryptionUtil) {
        ObjectMapper objectMapper = new ObjectMapper();
        
        try {
            return PatientQuestionnaireResponse.builder()
                    .questionnaireId(questionnaire.getQuestionnaireId())
                    .questionnaireName(questionnaire.getQuestionnaireName())
                    .realName(decryptedRealName)
                    .address(decryptedAddress)
                    .phoneNumber(decryptedPhoneNumber)
                    .issueDate(questionnaire.getIssueDate())
                    .lastModifiedDate(questionnaire.getLastModifiedDate())
                    .notes(questionnaire.getNotes())
                    .medicationInfo(parseEncryptedJsonToList(questionnaire.getMedicationInfo(), objectMapper, encryptionUtil))
                    .allergyInfo(parseEncryptedJsonToList(questionnaire.getAllergyInfo(), objectMapper, encryptionUtil))
                    .chronicDiseaseInfo(parseEncryptedJsonToList(questionnaire.getChronicDiseaseInfo(), objectMapper, encryptionUtil))
                    .surgeryHistoryInfo(parseEncryptedJsonToList(questionnaire.getSurgeryHistoryInfo(), objectMapper, encryptionUtil))
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

    private static List<Map<String, Object>> parseEncryptedJsonToList(String encryptedJson, ObjectMapper objectMapper, EncryptionUtil encryptionUtil) throws JsonProcessingException {
        if (encryptedJson == null || encryptedJson.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            // 암호화된 JSON을 복호화
            String decryptedJson = encryptionUtil.decrypt(encryptedJson);
            return objectMapper.readValue(decryptedJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            // 복호화 실패 시 원본 JSON으로 파싱 시도
            return parseJsonToList(encryptedJson, objectMapper);
        }
    }
} 