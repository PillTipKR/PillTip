package com.oauth2.User.PatientQuestionnaire.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireRequest;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireSummaryResponse;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.PatientQuestionnaire.Repository.PatientQuestionnaireRepository;
import com.oauth2.User.UserInfo.Service.UserService;
import com.oauth2.User.UserInfo.Service.UserSensitiveInfoService;
import com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class PatientQuestionnaireService {
    private final PatientQuestionnaireRepository questionnaireRepository;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final UserSensitiveInfoService userSensitiveInfoService;

    @Transactional
    public PatientQuestionnaire createQuestionnaire(User user, PatientQuestionnaireRequest request) throws JsonProcessingException {
        // Update user realName and address
        userService.updatePersonalInfo(user, request.getRealName(), request.getAddress());
        userService.updatePhoneNumber(user, request.getPhoneNumber());
        
        // 기존 민감정보 조회
        UserSensitiveInfoDto existingHistory = userSensitiveInfoService.getSensitiveInfo(user);
        
        // 기존 데이터와 새로운 데이터를 병합
        String medicationInfo = mergeWithExistingData(
            request.getMedicationInfo(), 
            existingHistory != null ? existingHistory.getMedicationInfo() : null, 
            "medicationName"
        );
        String allergyInfo = mergeWithExistingData(
            request.getAllergyInfo(), 
            existingHistory != null ? existingHistory.getAllergyInfo() : null, 
            "allergyName"
        );
        String chronicDiseaseInfo = mergeWithExistingData(
            request.getChronicDiseaseInfo(), 
            existingHistory != null ? existingHistory.getChronicDiseaseInfo() : null, 
            "chronicDiseaseName"
        );
        String surgeryHistoryInfo = mergeWithExistingData(
            request.getSurgeryHistoryInfo(), 
            existingHistory != null ? existingHistory.getSurgeryHistoryInfo() : null, 
            "surgeryHistoryName"
        );
        
        // 병합된 데이터를 user_sensitive_info에 저장
        userSensitiveInfoService.syncFromQuestionnaire(user, medicationInfo, allergyInfo, 
            chronicDiseaseInfo, surgeryHistoryInfo);
        
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
                        // medicationId인 경우 medicationName도 함께 저장
                        return Map.of(
                                keyName, value,
                                "medicationName", item.getMedicationName() != null ? item.getMedicationName() : "",
                                "submitted", item.isSubmitted()
                        );
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

    /**
     * 기존 데이터와 새로운 데이터를 병합
     */
    private String mergeWithExistingData(List<PatientQuestionnaireRequest.InfoItem> newItems, 
                                       List<String> existingItems, 
                                       String nameField) {
        // 기존 데이터를 Set으로 변환 (중복 체크용)
        Set<String> existingNames = (existingItems != null && !existingItems.isEmpty()) 
            ? new HashSet<>(existingItems)
            : new HashSet<>();
        
        // 새로운 데이터에서 이름 추출 (submitted 여부와 관계없이 모든 항목)
        List<String> newNames = new ArrayList<>();
        if (newItems != null) {
            for (PatientQuestionnaireRequest.InfoItem item : newItems) {
                String name = getNameFromItem(item, nameField);
                if (name != null && !name.trim().isEmpty() && !existingNames.contains(name)) {
                    newNames.add(name.trim());
                    existingNames.add(name.trim()); // 중복 방지를 위해 Set에 추가
                }
            }
        }
        
        // 기존 데이터와 새로운 데이터를 합침
        List<String> mergedList = new ArrayList<>();
        if (existingItems != null) {
            mergedList.addAll(existingItems);
        }
        mergedList.addAll(newNames);
        
        return String.join(", ", mergedList);
    }
    
    /**
     * InfoItem에서 이름 필드 값을 추출
     */
    private String getNameFromItem(PatientQuestionnaireRequest.InfoItem item, String nameField) {
        switch (nameField) {
            case "medicationName":
                return item.getMedicationName();
            case "allergyName":
                return item.getAllergyName();
            case "chronicDiseaseName":
                return item.getChronicDiseaseName();
            case "surgeryHistoryName":
                return item.getSurgeryHistoryName();
            default:
                return null;
        }
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
        PatientQuestionnaire questionnaire = questionnaireRepository.findByIdWithUser(id)
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
        // Update user realName and address
        userService.updatePersonalInfo(user, request.getRealName(), request.getAddress());
        userService.updatePhoneNumber(user, request.getPhoneNumber());
        
        // 기존 민감정보 조회
        UserSensitiveInfoDto existingHistory = userSensitiveInfoService.getSensitiveInfo(user);
        
        // 기존 데이터와 새로운 데이터를 병합
        String medicationInfo = mergeWithExistingData(
            request.getMedicationInfo(), 
            existingHistory != null ? existingHistory.getMedicationInfo() : null, 
            "medicationName"
        );
        String allergyInfo = mergeWithExistingData(
            request.getAllergyInfo(), 
            existingHistory != null ? existingHistory.getAllergyInfo() : null, 
            "allergyName"
        );
        String chronicDiseaseInfo = mergeWithExistingData(
            request.getChronicDiseaseInfo(), 
            existingHistory != null ? existingHistory.getChronicDiseaseInfo() : null, 
            "chronicDiseaseName"
        );
        String surgeryHistoryInfo = mergeWithExistingData(
            request.getSurgeryHistoryInfo(), 
            existingHistory != null ? existingHistory.getSurgeryHistoryInfo() : null, 
            "surgeryHistoryName"
        );
        
        // 병합된 데이터를 user_sensitive_info에 저장
        userSensitiveInfoService.syncFromQuestionnaire(user, medicationInfo, allergyInfo, 
            chronicDiseaseInfo, surgeryHistoryInfo);
        
        PatientQuestionnaire q = questionnaireRepository.findByIdWithUser(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        if (!q.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 수정할 수 있습니다.");
        }
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

    // 사용자의 최신 문진표 조회
    public PatientQuestionnaire getLatestQuestionnaireByUser(User user) {
        return questionnaireRepository.findTopByUserOrderByIssueDateDesc(user)
                .orElse(null);
    }

    // 소유자 검증 없이 문진표 조회
    public PatientQuestionnaire getQuestionnaireByIdPublic(Integer id) {
        return questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
    }
} 