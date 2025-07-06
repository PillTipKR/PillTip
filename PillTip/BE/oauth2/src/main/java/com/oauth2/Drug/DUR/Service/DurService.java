package com.oauth2.Drug.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Dto.DurAnalysisResponse;
import com.oauth2.Drug.DUR.Dto.DurDto;
import com.oauth2.Drug.DUR.Dto.DurPerDrugDto;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class DurService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final DrugRepository drugRepository;
    private final TakingPillService takingPillService;

    public DurAnalysisResponse generateTagsForDrugs(User user, long drugId1, long drugId2) throws JsonProcessingException {
        List<DurTagDto> tag1;
        List<DurTagDto> tag2;
        List<DurTagDto> interact;

        boolean isElderly = user.getUserProfile().getAge() >= 65;
        Map<String, List<Long>> classToDrugIdsMap = new HashMap<>();
        Set<String> userInteraction = new HashSet<>();

        //유저 약 정보 찾기
        List<Long> userDrugIds = takingPillService.getTakingPillSummary(user).getTakingPills().stream()
                .map(TakingPillSummaryResponse.TakingPillSummary::getMedicationId)
                .toList();

        //유저 약 interact 찾기용
        for (Long userDrugId : userDrugIds) {
            Optional<Drug> userDrug = drugRepository.findById(userDrugId);
            if (userDrug.isEmpty()) continue;
            String drugName = userDrug.get().getName();

            List<String> contraList = redisTemplate.opsForList().range("DUR:INTERACT:" + drugName, 0, -1);
            if(contraList != null)
                userInteraction.add(drugName);

            Map<String, String> value = readJsonFromRedis("DUR:THERAPEUTIC_DUP:" + userDrugId);
            if (value != null) {
                String className = value.getOrDefault("className", "").trim();
                if (!className.isBlank()) {
                    classToDrugIdsMap.computeIfAbsent(className, k -> new ArrayList<>()).add(userDrugId);
                }
            }
        }

        //약 비교
        Optional<Drug> drug1 = drugRepository.findById(drugId1);
        Optional<Drug> drug2 = drugRepository.findById(drugId2);
        tag1 = calDur(drug1,classToDrugIdsMap,userInteraction,
                isElderly,user.getUserProfile().isPregnant(), user.getUserProfile().getBirthDate());
        tag2 = calDur(drug2,classToDrugIdsMap,userInteraction,
                isElderly,user.getUserProfile().isPregnant(), user.getUserProfile().getBirthDate());
        interact = calInteract(drug1,drug2);
        return new DurAnalysisResponse(
                new DurPerDrugDto(
                        drug1.map(Drug::getName).orElse(null),
                        tag1.stream().filter(DurTagDto::isTrue).toList()),
                new DurPerDrugDto(
                        drug2.map(Drug::getName).orElse(null),
                        tag2.stream().filter(DurTagDto::isTrue).toList()),
                new DurPerDrugDto(
                        drug1.map(Drug::getName).orElse(null)
                        + drug2.map(Drug::getName).orElse(null),
                        interact.stream().filter(DurTagDto::isTrue).toList()),
                !userDrugIds.isEmpty()
        );
    }

    private List<DurTagDto> calDur(Optional<Drug> drug, Map<String, List<Long>> classToDrugIdsMap, Set<String> userInteraction,
                                   Boolean isElderly, Boolean isPregnant, LocalDate localDate) throws JsonProcessingException {

        List<DurTagDto> tags = new ArrayList<>();
        if(drug.isPresent()){
            Long drugId= drug.get().getId();
            String drugName = drug.get().getName();
            List<String> contraList = redisTemplate.opsForList().range("DUR:INTERACT:" + drugName, 0, -1);
            if (contraList != null)
                tags.add(buildContraTag(drug, userInteraction));

            tags.add(buildDurTag("임부금기", readJsonFromRedis("DUR:PREGNANCY:" + drugId), isPregnant));
            tags.add(buildDurTag("노인금기", readJsonFromRedis("DUR:ELDER:" + drugId), isElderly));

            Map<String, String> ageValue = readJsonFromRedis("DUR:AGE:" + drugId);
            boolean showAgeTag = ageValue != null && isUserInRestrictedAge(localDate, ageValue.get("conditionValue"));
            tags.add(buildDurTag("연령금기", ageValue, showAgeTag));

            Map<String, String> therValue = readJsonFromRedis("DUR:THERAPEUTIC_DUP:" + drugId);
            String className = therValue != null ? therValue.get("className") : null;
            boolean isDup = className != null && classToDrugIdsMap.containsKey(className);
            tags.add(buildDurTag("효능군중복주의", therValue, isDup));
        }
        return tags;
    }

    private List<DurTagDto> calInteract(Optional<Drug> drug1, Optional<Drug> drug2) throws JsonProcessingException {

        List<DurTagDto> tags = new ArrayList<>();
        if(drug1.isPresent() && drug2.isPresent()){
            Long drugId1= drug1.get().getId();
            String drugName1 = drug1.get().getName();
            Long drugId2= drug2.get().getId();
            String drugName2 = drug2.get().getName();

            List<String> contraList = redisTemplate.opsForList().range("DUR:INTERACT:" + drugName1, 0, -1);
            if (contraList != null){
                List<DurDto> tagDesc = new ArrayList<>();
                String detailKey = "DUR:INTERACT_DETAIL:" + drugName1 + ":" + drugName2;
                Map<String, String> detail = readJsonFromRedis(detailKey);
                if (detail != null) {
                    tagDesc.add(new DurDto(
                            drugName1 + " + " + drugName2,
                            detail.getOrDefault("reason", ""),
                            detail.getOrDefault("note", "")
                    ));
                }
                tags.add(new DurTagDto("병용금기", tagDesc, !tagDesc.isEmpty()));
            }

            Map<String, String> therValue = readJsonFromRedis("DUR:THERAPEUTIC_DUP:" + drugId1);
            Map<String, String> value = readJsonFromRedis("DUR:THERAPEUTIC_DUP:" + drugId2);

            boolean isDup = therValue != null
                    && value != null
                    && therValue.get("className") != null
                    && value.containsKey(therValue.get("className"));
            tags.add(buildDurTag("효능군중복주의", therValue, isDup));
        }
        return tags;
    }


    private Map<String, String> readJsonFromRedis(String key) throws JsonProcessingException {
        String json = redisTemplate.opsForValue().get(key);
        return (json != null) ? objectMapper.readValue(json, new TypeReference<>() {}) : null;
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

    private DurTagDto buildContraTag(Optional<Drug> drug, Set<String> userInteraction) throws JsonProcessingException {
        List<DurDto> tagDesc = new ArrayList<>();
        if(drug.isPresent()) {
            String drugName = drug.get().getName();

            for (String otherName : userInteraction) {
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
