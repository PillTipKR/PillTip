package com.oauth2.User.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.dto.PatientQuestionnaireRequest;
import com.oauth2.User.dto.PatientQuestionnaireSummaryResponse;
import com.oauth2.User.entity.PatientQuestionnaire;
import com.oauth2.User.entity.User;
import com.oauth2.User.repository.PatientQuestionnaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientQuestionnaireService {
    private final PatientQuestionnaireRepository questionnaireRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public PatientQuestionnaire createQuestionnaire(User user, PatientQuestionnaireRequest request) throws JsonProcessingException {
        String medicationInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getMedicationInfo(), "medicationId")
        );
        String allergyInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getAllergyInfo(), "allergyName")
        );
        String chronicDiseaseInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getChronicDiseaseInfo(), "chronicDiseaseName")
        );
        String surgeryHistoryInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getSurgeryHistoryInfo(), "surgeryHistoryName")
        );

        PatientQuestionnaire questionnaire = PatientQuestionnaire.builder()
                .user(user)
                .realName(request.getRealName())
                .address(request.getAddress())
                .questionnaireName(request.getQuestionnaireName())
                .notes(request.getNotes())
                .issueDate(LocalDate.now())
                .lastModifiedDate(LocalDate.now())
                .medicationInfo(medicationInfoJson)
                .allergyInfo(allergyInfoJson)
                .chronicDiseaseInfo(chronicDiseaseInfoJson)
                .surgeryHistoryInfo(surgeryHistoryInfoJson)
                .build();
        return questionnaireRepository.save(questionnaire);
    }

    private List<Map<String, ?>> toKeyedList(List<PatientQuestionnaireRequest.InfoItem> list, String keyName) {
        if (list == null) return null;
        return list.stream()
                .map(item -> {
                    String value = null;
                    if ("medicationId".equals(keyName)) {
                        value = item.getMedicationId();
                    } else if ("allergyName".equals(keyName)) {
                        value = item.getAllergyName();
                    } else if ("chronicDiseaseName".equals(keyName)) {
                        value = item.getChronicDiseaseName();
                    } else if ("surgeryHistoryName".equals(keyName)) {
                        value = item.getSurgeryHistoryName();
                    } else {
                        value = item.getId();
                    }
                    return Map.of(
                            keyName, value,
                            "submitted", item.isSubmitted()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<PatientQuestionnaireSummaryResponse> getUserQuestionnaireSummaries(User user) {
        return questionnaireRepository.findByUser(user).stream()
            .map(q -> new PatientQuestionnaireSummaryResponse(
                q.getQuestionnaireId(),
                q.getQuestionnaireName(),
                q.getIssueDate(),
                q.getLastModifiedDate()
            ))
            .collect(Collectors.toList());
    }

    public PatientQuestionnaire getQuestionnaireById(User user, Integer id) {
        PatientQuestionnaire questionnaire = questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        
        // 본인 문진표만 조회 가능
        if (!questionnaire.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 조회할 수 있습니다.");
        }
        
        return questionnaire;
    }

    @Transactional
    public void deleteQuestionnaire(User user, Integer id) {
        PatientQuestionnaire q = questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        if (!q.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 삭제할 수 있습니다.");
        }
        questionnaireRepository.delete(q);
    }

    @Transactional
    public PatientQuestionnaire updateQuestionnaire(User user, Integer id, PatientQuestionnaireRequest request) throws JsonProcessingException {
        PatientQuestionnaire q = questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        if (!q.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 수정할 수 있습니다.");
        }
        q.setRealName(request.getRealName());
        q.setAddress(request.getAddress());
        q.setQuestionnaireName(request.getQuestionnaireName());
        q.setNotes(request.getNotes());
        q.setMedicationInfo(objectMapper.writeValueAsString(toKeyedList(request.getMedicationInfo(), "medicationId")));
        q.setAllergyInfo(objectMapper.writeValueAsString(toKeyedList(request.getAllergyInfo(), "allergyName")));
        q.setChronicDiseaseInfo(objectMapper.writeValueAsString(toKeyedList(request.getChronicDiseaseInfo(), "chronicDiseaseName")));
        q.setSurgeryHistoryInfo(objectMapper.writeValueAsString(toKeyedList(request.getSurgeryHistoryInfo(), "surgeryHistoryName")));
        q.setLastModifiedDate(LocalDate.now());
        return q;
    }

    @Transactional
    public List<PatientQuestionnaireSummaryResponse> deleteQuestionnaireAndReturnList(User user, Integer id) {
        PatientQuestionnaire q = questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        if (!q.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 삭제할 수 있습니다.");
        }
        questionnaireRepository.delete(q);
        // 삭제 후 남아있는 리스트 반환
        return getUserQuestionnaireSummaries(user);
    }
} 