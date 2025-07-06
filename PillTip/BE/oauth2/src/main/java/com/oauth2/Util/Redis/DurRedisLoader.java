package com.oauth2.Util.Redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.DUR.Domain.DrugCaution;
import com.oauth2.Drug.DUR.Domain.DrugInteraction;
import com.oauth2.Drug.DUR.Domain.DrugTherapeuticDup;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DUR.Repository.DrugCautionRepository;
import com.oauth2.Drug.DUR.Repository.DrugInteractionRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.DUR.Repository.DrugTherapeuticDupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DurRedisLoader {

    private final StringRedisTemplate redisTemplate;
    private final DrugInteractionRepository interactionRepo;
    private final DrugCautionRepository cautionRepo;
    private final DrugTherapeuticDupRepository dupRepo;
    private final ObjectMapper objectMapper;
    private final DrugRepository drugRepository;

    public void loadAll() throws JsonProcessingException {
        saveInteractions();
        saveCautions();
        saveTherapeuticDups();
    }

    private void saveInteractions() throws JsonProcessingException {
        List<DrugInteraction> interactions = interactionRepo.findAll();
        Map<String, List<String>> map = new HashMap<>();
        Map<Long, String> drugIdNameMap = drugRepository.findAll().stream()
                .collect(Collectors.toMap(Drug::getId, Drug::getName));

        for (DrugInteraction di : interactions) {
            map.computeIfAbsent(drugIdNameMap.get(di.getDrugId1()), k -> new ArrayList<>()).add(drugIdNameMap.get(di.getDrugId2()));
            // 상세 정보 저장
            String key1 = "DUR:INTERACT_DETAIL:" + drugIdNameMap.get(di.getDrugId1()) + ":" + drugIdNameMap.get(di.getDrugId2());

            Map<String, String> value = Map.of(
                    "reason", di.getReason() == null ? "" : di.getReason(),
                    "note", di.getNote() == null || di.getNote().equals("없음") ? "" : di.getNote()
            );

            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key1, json);
        }

        for (var entry : map.entrySet()) {
            String key = "DUR:INTERACT:" + entry.getKey();
            List<String> ids = entry.getValue().stream().map(String::valueOf).toList();
            redisTemplate.delete(key);
            redisTemplate.opsForList().rightPushAll(key, ids);
        }
    }

    private void saveCautions() throws JsonProcessingException {
        List<DrugCaution> cautions = cautionRepo.findAll();

        for (DrugCaution dc : cautions) {
            String key = "DUR:" + dc.getConditionType().name() + ":" + dc.getDrugId();
            Map<String, String> value = Map.of(
                    "conditionValue", dc.getConditionValue() == null ? "" : dc.getConditionValue(),
                    "note", dc.getNote() == null ? "" : dc.getNote()
            );
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        }
    }

    private void saveTherapeuticDups() throws JsonProcessingException {
        List<DrugTherapeuticDup> dups = dupRepo.findAll();

        for (DrugTherapeuticDup dup : dups) {
            String key = "DUR:THERAPEUTIC_DUP:" + dup.getDrugId();
            Map<String, String> value = Map.of(
                    "category", dup.getCategory(),
                    "className", dup.getClassName(),
                    "note", dup.getNote() == null ? "" : dup.getNote(),
                    "remark", dup.getRemark() == null ? "" : dup.getRemark()
            );
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        }
    }
}
