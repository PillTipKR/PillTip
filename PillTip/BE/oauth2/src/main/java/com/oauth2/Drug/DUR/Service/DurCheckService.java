package com.oauth2.Drug.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Dto.DurDto;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DUR.Dto.DurUserContext;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DurCheckService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final DrugRepository drugRepository;
    private final TakingPillService takingPillService;

    public List<DurTagDto> checkForDrug(Drug drug, UserProfile userProfile, DurUserContext userContext) throws JsonProcessingException {
        List<DurTagDto> tags = new ArrayList<>();
        Long drugId = drug.getId();
        String drugName = drug.getName();

        // 병용금기 (사용자가 복용중인 다른 약물과의 상호작용)
        tags.add(buildContraTag(drugName, userContext.userInteractionDrugNames()));

        // 임부금기
        tags.add(buildDurTag("임부금기", readJsonFromRedis("DUR:PREGNANCY:" + drugId), userProfile.isPregnant()));

        // 노인금기
        tags.add(buildDurTag("노인금기", readJsonFromRedis("DUR:ELDER:" + drugId), userContext.isElderly()));

        // 연령금기
        Map<String, String> ageValue = readJsonFromRedis("DUR:AGE:" + drugId);
        boolean showAgeTag = ageValue != null && isUserInRestrictedAge(userProfile.getBirthDate(), ageValue.get("conditionValue"));
        tags.add(buildDurTag("연령금기", ageValue, showAgeTag));

        // 효능군 중복주의
        Map<String, String> therValue = readJsonFromRedis("DUR:THERAPEUTIC_DUP:" + drugId);
        String className = therValue != null ? therValue.get("className") : null;
        boolean isDup = className != null && userContext.classToDrugIdsMap().containsKey(className);
        tags.add(buildDurTag("효능군중복주의", therValue, isDup));

        return tags;
    }

    private Map<String, String> readJsonFromRedis(String key) throws JsonProcessingException {
        String json = redisTemplate.opsForValue().get(key);
        return (json != null) ? objectMapper.readValue(json, new TypeReference<>() {}) : null;
    }

    public DurUserContext buildUserContext(User user) throws JsonProcessingException {
        boolean isElderly = user.getUserProfile().getAge() >= 65;
        Map<String, List<Long>> classToDrugIdsMap = new HashMap<>();
        Set<String> userInteractionDrugNames = new HashSet<>();

        List<Long> userDrugIds = takingPillService.getTakingPillSummary(user).getTakingPills().stream()
                .map(TakingPillSummaryResponse.TakingPillSummary::getMedicationId)
                .toList();

        for (Long userDrugId : userDrugIds) {
            Optional<Drug> userDrugOpt = drugRepository.findById(userDrugId);
            if (userDrugOpt.isEmpty()) continue;

            String drugName = userDrugOpt.get().getName();
            List<String> contraList = redisTemplate.opsForList().range("DUR:INTERACT:" + drugName, 0, -1);
            if (contraList != null && !contraList.isEmpty()) {
                userInteractionDrugNames.add(drugName);
            }

            Map<String, String> value = readJsonFromRedis("DUR:THERAPEUTIC_DUP:" + userDrugId);
            if (value != null) {
                String className = value.getOrDefault("className", "").trim();
                if (!className.isBlank()) {
                    classToDrugIdsMap.computeIfAbsent(className, k -> new ArrayList<>()).add(userDrugId);
                }
            }
        }
        return new DurUserContext(isElderly, user.getUserProfile().isPregnant(), classToDrugIdsMap, userInteractionDrugNames);
    }

    private DurTagDto buildDurTag(String tagName, Map<String, String> valueMap, boolean shouldTag) {
        List<DurDto> list = new ArrayList<>();
        if (shouldTag && valueMap != null && !valueMap.isEmpty()) {
            list.add(new DurDto(
                    valueMap.getOrDefault("category", ""),
                    valueMap.getOrDefault("conditionValue", valueMap.getOrDefault("remark", "")),
                    valueMap.getOrDefault("note", "")
            ));
        }
        return new DurTagDto(tagName, list, shouldTag && !list.isEmpty());
    }

    private DurTagDto buildContraTag(String drugName, Set<String> userInteractionDrugNames) throws JsonProcessingException {
        List<DurDto> tagDesc = new ArrayList<>();
        for (String otherName : userInteractionDrugNames) {
            String detailKey = "DUR:INTERACT_DETAIL:" + drugName + ":" + otherName;
            Map<String, String> detail = readJsonFromRedis(detailKey);
            if (detail != null) {
                tagDesc.add(new DurDto(
                        drugName + " + " + otherName,
                        detail.getOrDefault("reason", ""),
                        detail.getOrDefault("note", "")
                ));
            }
        }
        return new DurTagDto("병용금기", tagDesc, !tagDesc.isEmpty());
    }

    private boolean isUserInRestrictedAge(LocalDate birthDate, String conditionValue) {
        if (conditionValue == null || birthDate == null || conditionValue.isBlank()) return false;
        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();
        int ageInMonths = age * 12 + Period.between(birthDate, today).getMonths();

        String[] parts = conditionValue.split("\\s*,\\s*");
        for (String part : parts) {
            int limit;
            try {
                limit = Integer.parseInt(part.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                continue;
            }

            if (part.contains("개월 미만") && ageInMonths < limit) return true;
            if (part.contains("개월 이하") && ageInMonths <= limit) return true;
            if (part.contains("개월 초과") && ageInMonths > limit) return true;
            if (part.contains("개월 이상") && ageInMonths >= limit) return true;
            if (part.contains("세 미만") && age < limit) return true;
            if (part.contains("세 이하") && age <= limit) return true;
            if (part.contains("세 초과") && age > limit) return true;
            if (part.contains("세 이상") && age >= limit) return true;
        }
        return false;
    }
}
