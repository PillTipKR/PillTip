package com.oauth2.Drug.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Dto.DurDto;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DUR.Dto.SearchDurDto;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.Search.Dto.SearchIndexDTO;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DurTaggingService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final DrugRepository drugRepository;
    private final TakingPillService takingPillService;

    public List<SearchDurDto> generateTagsForDrugs(User user, List<SearchIndexDTO> drugs) throws JsonProcessingException {
        List<SearchDurDto> result = new ArrayList<>();
        boolean isElderly = user.getUserProfile().getAge() >= 65;
        Map<String, List<Long>> classToDrugIdsMap = new HashMap<>();
        Set<String> userInteraction = new HashSet<>();

        List<Long> userDrugIds = takingPillService.getTakingPillSummary(user).getTakingPills().stream()
                .map(TakingPillSummaryResponse.TakingPillSummary::getMedicationId)
                .toList();

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

        for (SearchIndexDTO drug : drugs) {
            Long drugId = drug.id();
            String drugName = drug.drugName();
            List<DurTagDto> tags = new ArrayList<>();

            List<String> contraList = redisTemplate.opsForList().range("DUR:INTERACT:" + drugName, 0, -1);
            if(contraList != null)
                tags.add(buildContraTag(drug, userInteraction));

            tags.add(buildDurTag("임부금기", readJsonFromRedis("DUR:PREGNANCY:" + drugId), user.getUserProfile().isPregnant()));
            tags.add(buildDurTag("노인금기", readJsonFromRedis("DUR:ELDER:" + drugId), isElderly));

            Map<String, String> ageValue = readJsonFromRedis("DUR:AGE:" + drugId);
            boolean showAgeTag = ageValue != null && isUserInRestrictedAge(user.getUserProfile().getBirthDate(), ageValue.get("conditionValue"));
            tags.add(buildDurTag("연령금기", ageValue, showAgeTag));

            Map<String, String> therValue = readJsonFromRedis("DUR:THERAPEUTIC_DUP:" + drugId);
            boolean isDup = therValue != null && classToDrugIdsMap.containsKey(therValue.get("className"));
            tags.add(buildDurTag("효능군중복주의", therValue, isDup));

            Optional<Drug> drug1  = drugRepository.findById(drugId);

            String image = drug1.map(Drug::getImage).orElse(null);
            result.add(new SearchDurDto(drugId, drugName, drug.ingredient(), drug.manufacturer(), image, tags));
        }
        return result;
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

    private DurTagDto buildContraTag(SearchIndexDTO drug, Set<String> userInteraction) throws JsonProcessingException {
        List<DurDto> tagDesc = new ArrayList<>();
        String drugName = drug.drugName();

        for (String otherName : userInteraction) {
            String detailKey = "DUR:INTERACT_DETAIL:" + drugName + ":" + otherName;
            Map<String, String> detail = readJsonFromRedis(detailKey);
            if(detail != null) {
                tagDesc.add(new DurDto(
                        drugName+ " + " + otherName,
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
