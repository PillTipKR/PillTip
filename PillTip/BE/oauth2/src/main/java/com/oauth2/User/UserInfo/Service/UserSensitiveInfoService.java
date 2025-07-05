// author : mireutale
// description : 사용자 민감정보 서비스
package com.oauth2.User.UserInfo.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDto;
import com.oauth2.User.UserInfo.Dto.UserSensitiveInfoDeleteRequest;
import com.oauth2.User.UserInfo.Entity.UserSensitiveInfo;
import com.oauth2.User.UserInfo.Repository.UserSensitiveInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserSensitiveInfoService {

    private final UserSensitiveInfoRepository userSensitiveInfoRepository;
    private final ObjectMapper objectMapper;

    /**
     * 사용자 민감정보 저장 또는 업데이트
     */
    @Transactional
    public UserSensitiveInfoDto saveOrUpdateSensitiveInfo(User user, List<String> medicationInfo,
                                                         List<String> allergyInfo,
                                                         List<String> chronicDiseaseInfo,
                                                         List<String> surgeryHistoryInfo) throws JsonProcessingException {
        UserSensitiveInfo sensitiveInfo = userSensitiveInfoRepository.findByUser(user)
                .orElse(new UserSensitiveInfo());

        sensitiveInfo.setUser(user);
        sensitiveInfo.setMedicationInfo(convertListToCommaSeparated(medicationInfo));
        sensitiveInfo.setAllergyInfo(convertListToCommaSeparated(allergyInfo));
        sensitiveInfo.setChronicDiseaseInfo(convertListToCommaSeparated(chronicDiseaseInfo));
        sensitiveInfo.setSurgeryHistoryInfo(convertListToCommaSeparated(surgeryHistoryInfo));

        UserSensitiveInfo saved = userSensitiveInfoRepository.save(sensitiveInfo);
        return UserSensitiveInfoDto.from(saved);
    }

    /**
     * 사용자 민감정보 조회
     */
    public UserSensitiveInfoDto getSensitiveInfo(User user) {
        UserSensitiveInfo sensitiveInfo = userSensitiveInfoRepository.findByUser(user)
                .orElse(null);
        return sensitiveInfo != null ? UserSensitiveInfoDto.from(sensitiveInfo) : null;
    }

    /**
     * 사용자 민감정보 존재 여부 확인
     */
    public boolean existsByUser(User user) {
        return userSensitiveInfoRepository.existsByUser(user);
    }

    /**
     * 사용자 민감정보 전체 삭제
     */
    @Transactional
    public void deleteAllSensitiveInfo(User user) {
        userSensitiveInfoRepository.deleteByUser(user);
    }

    /**
     * 특정 카테고리의 민감정보만 업데이트
     */
    @Transactional
    public UserSensitiveInfoDto updateSensitiveInfoCategory(User user, String category, List<String> data) throws JsonProcessingException {
        UserSensitiveInfo sensitiveInfo = userSensitiveInfoRepository.findByUser(user)
                .orElse(new UserSensitiveInfo());

        sensitiveInfo.setUser(user);

        switch (category.toLowerCase()) {
            case "medication":
                sensitiveInfo.setMedicationInfo(convertListToCommaSeparated(data));
                break;
            case "allergy":
                sensitiveInfo.setAllergyInfo(convertListToCommaSeparated(data));
                break;
            case "chronicdisease":
                sensitiveInfo.setChronicDiseaseInfo(convertListToCommaSeparated(data));
                break;
            case "surgeryhistory":
                sensitiveInfo.setSurgeryHistoryInfo(convertListToCommaSeparated(data));
                break;
            default:
                throw new IllegalArgumentException("Invalid category: " + category);
        }

        UserSensitiveInfo saved = userSensitiveInfoRepository.save(sensitiveInfo);
        return UserSensitiveInfoDto.from(saved);
    }

    /**
     * 문진표에서 민감정보 동기화
     */
    @Transactional
    public UserSensitiveInfoDto syncFromQuestionnaire(User user, String medicationInfo, String allergyInfo,
                                                     String chronicDiseaseInfo, String surgeryHistoryInfo) {
        UserSensitiveInfo sensitiveInfo = userSensitiveInfoRepository.findByUser(user)
                .orElse(new UserSensitiveInfo());

        sensitiveInfo.setUser(user);
        sensitiveInfo.setMedicationInfo(medicationInfo != null ? medicationInfo : "");
        sensitiveInfo.setAllergyInfo(allergyInfo != null ? allergyInfo : "");
        sensitiveInfo.setChronicDiseaseInfo(chronicDiseaseInfo != null ? chronicDiseaseInfo : "");
        sensitiveInfo.setSurgeryHistoryInfo(surgeryHistoryInfo != null ? surgeryHistoryInfo : "");

        UserSensitiveInfo saved = userSensitiveInfoRepository.save(sensitiveInfo);
        return UserSensitiveInfoDto.from(saved);
    }

    /**
     * 사용자 민감정보 선택적 삭제 (boolean으로 지정된 카테고리만 유지)
     */
    @Transactional
    public UserSensitiveInfoDto deleteSensitiveInfoCategories(User user, UserSensitiveInfoDeleteRequest request) throws JsonProcessingException {
        UserSensitiveInfo sensitiveInfo = userSensitiveInfoRepository.findByUser(user)
                .orElse(null);

        if (sensitiveInfo == null) {
            return null;
        }

        if (!request.isKeepMedicationInfo()) {
            sensitiveInfo.setMedicationInfo("");
        }
        if (!request.isKeepAllergyInfo()) {
            sensitiveInfo.setAllergyInfo("");
        }
        if (!request.isKeepChronicDiseaseInfo()) {
            sensitiveInfo.setChronicDiseaseInfo("");
        }
        if (!request.isKeepSurgeryHistoryInfo()) {
            sensitiveInfo.setSurgeryHistoryInfo("");
        }

        UserSensitiveInfo saved = userSensitiveInfoRepository.save(sensitiveInfo);
        return UserSensitiveInfoDto.from(saved);
    }

    /**
     * 사용자 민감정보 전체 삭제
     */
    @Transactional
    public void deleteAllSensitiveInfoByUser(User user) {
        userSensitiveInfoRepository.deleteByUser(user);
    }

    private String convertListToCommaSeparated(List<String> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        
        return data.stream()
                .filter(item -> item != null && !item.trim().isEmpty())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
} 