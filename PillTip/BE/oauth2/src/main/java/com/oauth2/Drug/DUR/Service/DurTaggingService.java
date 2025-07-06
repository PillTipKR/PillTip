package com.oauth2.Drug.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Dto.DurTagDto;
import com.oauth2.Drug.DUR.Dto.SearchDurDto;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.Search.Dto.SearchIndexDTO;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.TakingPill.Dto.TakingPillSummaryResponse;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DurTaggingService {

    private final DrugRepository drugRepository;
    private final TakingPillService takingPillService;
    private final DurCheckService durCheckService; // 새로 추가된 서비스
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public List<SearchDurDto> generateTagsForDrugs(User user, List<SearchIndexDTO> drugs) throws JsonProcessingException {
        DurUserContext userContext = buildUserContext(user);

        List<Long> drugIds = drugs.stream().map(SearchIndexDTO::id).toList();
        Map<Long, Drug> drugMap = drugRepository.findAllById(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, drug -> drug));

        List<SearchDurDto> result = new ArrayList<>();
        for (SearchIndexDTO drugDto : drugs) {
            Drug drug = drugMap.get(drugDto.id());
            if (drug == null) continue; // 약 정보를 찾을 수 없는 경우 건너뛰기
            List<DurTagDto> tags = durCheckService.checkForDrug(drug, user.getUserProfile(), userContext);

            result.add(new SearchDurDto(
                    drug.getId(),
                    drug.getName(),
                    drugDto.ingredient(),
                    drug.getManufacturer(),
                    drug.getImage(),
                    tags
            ));
        }
        return result;
    }

    private DurUserContext buildUserContext(User user) throws JsonProcessingException {
        boolean isElderly = user.getUserProfile().getAge() >= 65;
        Map<String, List<Long>> classToDrugIdsMap = new HashMap<>();
        Set<String> userInteractionDrugNames = new HashSet<>();

        List<Long> userDrugIds = takingPillService.getTakingPillSummary(user).getTakingPills().stream()
                .map(TakingPillSummaryResponse.TakingPillSummary::getMedicationId)
                .toList();

        for (Long userDrugId : userDrugIds) {
            Optional<Drug> userDrugOpt = drugRepository.findById(userDrugId);
            if (userDrugOpt.isEmpty()) continue;

            Drug userDrug = userDrugOpt.get();
            String drugName = userDrug.getName();

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

    private Map<String, String> readJsonFromRedis(String key) throws JsonProcessingException {
        String json = redisTemplate.opsForValue().get(key);
        return (json != null) ? objectMapper.readValue(json, new TypeReference<>() {}) : null;
    }
}
