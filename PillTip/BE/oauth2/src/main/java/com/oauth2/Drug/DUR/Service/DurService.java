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

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class DurService {

    private final DrugRepository drugRepository;
    private final TakingPillService takingPillService;
    private final DurCheckService durCheckService; // 새로 추가된 서비스
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public DurAnalysisResponse generateTagsForDrugs(User user, long drugId1, long drugId2) throws JsonProcessingException {
        Optional<Drug> drug1Opt = drugRepository.findById(drugId1);
        Optional<Drug> drug2Opt = drugRepository.findById(drugId2);

        if (drug1Opt.isEmpty() || drug2Opt.isEmpty()) {
            // 약 정보를 찾을 수 없는 경우 예외 처리 또는 기본 응답 반환
            throw new NoSuchElementException("One or both drugs not found");
        }

        DurUserContext userContext = buildUserContext(user);

        List<DurTagDto> tagsForDrug1 = durCheckService.checkForDrug(drug1Opt.get(), user.getUserProfile(), userContext);
        List<DurTagDto> tagsForDrug2 = durCheckService.checkForDrug(drug2Opt.get(), user.getUserProfile(), userContext);
        List<DurTagDto> interactionTags = checkInteractionBetweenTwoDrugs(drug1Opt.get(), drug2Opt.get());

        return new DurAnalysisResponse(
                new DurPerDrugDto(
                        drug1Opt.get().getName(),
                        tagsForDrug1.stream().filter(DurTagDto::isTrue).toList()),
                new DurPerDrugDto(
                        drug2Opt.get().getName(),
                        tagsForDrug2.stream().filter(DurTagDto::isTrue).toList()),
                new DurPerDrugDto(
                        drug1Opt.get().getName() + " + " + drug2Opt.get().getName(),
                        interactionTags.stream().filter(DurTagDto::isTrue).toList()),
                !userContext.userInteractionDrugNames().isEmpty()
        );
    }

    private DurUserContext buildUserContext(User user) throws JsonProcessingException {
        boolean isElderly = user.getUserProfile().getAge() >= 65;
        Map<String, List<Long>> classToDrugIdsMap = new HashMap<>();
        Set<String> userInteractionDrugNames = new HashSet<>();

        List<Long> userDrugIds = takingPillService.getTakingPillSummary(user).getTakingPills().stream()
                .map(TakingPillSummaryResponse.TakingPillSummary::getMedicationId)
                .toList();

        for (Long userDrugId : userDrugIds) {
            Optional<Drug> userDrug = drugRepository.findById(userDrugId);
            if (userDrug.isEmpty()) continue;

            String drugName = userDrug.get().getName();
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

    private List<DurTagDto> checkInteractionBetweenTwoDrugs(Drug drug1, Drug drug2) throws JsonProcessingException {
        List<DurTagDto> tags = new ArrayList<>();
        String drugName1 = drug1.getName();
        String drugName2 = drug2.getName();

        // 병용금기 확인
        List<String> contraList = redisTemplate.opsForList().range("DUR:INTERACT:" + drugName1, 0, -1);
        if (contraList != null && contraList.contains(drugName2)) {
            String detailKey = "DUR:INTERACT_DETAIL:" + drugName1 + ":" + drugName2;
            Map<String, String> detail = readJsonFromRedis(detailKey);
            List<DurDto> tagDesc = new ArrayList<>();
            if (detail != null) {
                tagDesc.add(new DurDto(
                        drugName1 + " + " + drugName2,
                        detail.getOrDefault("reason", ""),
                        detail.getOrDefault("note", "")
                ));
            }
            tags.add(new DurTagDto("병용금기", tagDesc, !tagDesc.isEmpty()));
        }

        // 효능군 중복 확인
        Map<String, String> therValue1 = readJsonFromRedis("DUR:THERAPEUTIC_DUP:" + drug1.getId());
        Map<String, String> therValue2 = readJsonFromRedis("DUR:THERAPEUTIC_DUP:" + drug2.getId());

        if (therValue1 != null && therValue2 != null) {
            String className1 = therValue1.get("className");
            String className2 = therValue2.get("className");
            if (className1 != null && className1.equals(className2)) {
                List<DurDto> list = new ArrayList<>();
                list.add(new DurDto(
                    therValue1.getOrDefault("category", ""),
                    therValue1.getOrDefault("conditionValue", therValue1.getOrDefault("remark", "")),
                    therValue1.getOrDefault("note", "")
                ));
                tags.add(new DurTagDto("효능군중복주의", list, true));
            }
        }
        return tags;
    }

    private Map<String, String> readJsonFromRedis(String key) throws JsonProcessingException {
        String json = redisTemplate.opsForValue().get(key);
        return (json != null) ? objectMapper.readValue(json, new TypeReference<>() {}) : null;
    }
}
