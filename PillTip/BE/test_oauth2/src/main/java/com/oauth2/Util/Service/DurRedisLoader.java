package com.oauth2.Util.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.Drug.Domain.*;
import com.oauth2.Drug.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class DurRedisLoader {

    private final DrugInteractionRepository interactionRepository;
    private final DrugTherapeuticDupRepository dupRepository;
    private final DrugCautionRepository cautionRepository;
    private final IngredientRepository ingredientRepository;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;


    /** 2. 효능군 중복 **/
    public void loadTherapeuticDups() {
        List<DrugTherapeuticDup> allDup = dupRepository.findAll();
        for (DrugTherapeuticDup dup : allDup) {
            Optional<Ingredient> ing = ingredientRepository.findById(dup.getIngredientId());
            if (ing.isEmpty()) continue;

            String key = "dup:" + ing.get().getNameKr();

            Map<String, String> value = new HashMap<>();
            value.put("className", dup.getClassName());
            value.put("category", dup.getCategory());
            value.put("note", dup.getNote());

            redisTemplate.opsForHash().putAll(key, value);
        }

        System.out.println("✅ 효능군 중복 Redis 저장 완료");
    }

    /** 3. 복약 주의사항 **/
    public void loadCautions() throws JsonProcessingException {
        List<DrugCaution> cautions = cautionRepository.findAll();

        Map<String, List<DrugCaution>> cautionMap = new HashMap<>();

        for (DrugCaution caution : cautions) {
            Optional<Ingredient> ing = ingredientRepository.findById(caution.getIngredientId());
            if (ing.isEmpty()) continue;

            String name = ing.get().getNameKr().trim();  // 성분명 기준
            cautionMap.computeIfAbsent(name, k -> new ArrayList<>()).add(caution);  // 같은 이름끼리 List로 묶음
        }

        for (Map.Entry<String, List<DrugCaution>> entry : cautionMap.entrySet()) {
            String key = "caution:" + entry.getKey();  // Redis key = caution:이부프로펜 같은 형식
            String json = objectMapper.writeValueAsString(entry.getValue());  // List<DrugCaution> → JSON

            redisTemplate.opsForValue().set(key, json);  // Redis에 저장
        }

        System.out.println("✅ 복약 주의사항 Redis 저장 완료");
    }

    /** 전체 DUR 적재 **/
    public void loadAll() throws JsonProcessingException {
        loadTherapeuticDups();
        loadCautions();
        System.out.println("🎉 모든 DUR Redis 적재 완료");
    }
}