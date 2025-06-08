package com.oauth2.DUR.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.DUR.Dto.DurDetail;
import com.oauth2.DUR.Dto.DurTagDto;
import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Repository.DrugRepository;
import com.oauth2.Search.Dto.SearchIndexDTO;
import com.oauth2.User.dto.TakingPillRequest;
import com.oauth2.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DurTaggingService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final DrugRepository drugRepository;

    public List<DurTagDto> generateTagsForDrugs(User user, List<SearchIndexDTO> drugs) throws JsonProcessingException {
        List<DurTagDto> result = new ArrayList<>();
        boolean isElderly = user.getUserProfile().getAge() <= 65;
        // 효능군 정보: className → 약물 ID 목록
        Map<String, List<Long>> classToDrugIdsMap = new HashMap<>();


        // 1. 복용 중인 약 정보 JSON 문자열 → DTO 리스트로 변환
        String pillsJson = user.getUserProfile().getTakingPills();
        List<TakingPillRequest> pillRequests = new ArrayList<>();
        if (pillsJson != null && !pillsJson.isEmpty())
            pillRequests = objectMapper.readValue(pillsJson, new TypeReference<>() {
            });

        // 2. 내가 복용 중인 약물 ID 목록
        Set<Long> userDrugIds = pillRequests.stream()
                .map(TakingPillRequest::getMedicationId)
                .collect(Collectors.toSet());

        Set<String> userDrugNames = pillRequests.stream()
                .map(TakingPillRequest::getMedicationName)
                .collect(Collectors.toSet());


        //사용자 복약정보에서 정보 들고오기
        for (Long userDrugId : userDrugIds) {

            String key = "DUR:THERAPEUTIC_DUP:" + userDrugId;
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) continue;

            Map<String, String> value = objectMapper.readValue(json, new TypeReference<>() {});
            String className = value.getOrDefault("className", "").trim();

            if (!className.isBlank()) {
                classToDrugIdsMap.computeIfAbsent(className, k -> new ArrayList<>()).add(userDrugId);
            }
        }

        for (SearchIndexDTO drug : drugs) {
            Long drugId = drug.id();
            String drugName = drug.drugName();
            List<DurDetail> tags = new ArrayList<>();

            Map<String, List<String>> contraMap = new HashMap<>();

            List<String> contraList = redisTemplate.opsForList()
                    .range("DUR:INTERACT:" + drugName, 0, -1);

            if(contraList != null) {
                for (String contraStr : contraList) {
                    contraMap.computeIfAbsent(contraStr, k -> new ArrayList<>()).add(drugName);
                }

                // contraMap의 key가 userDrugIds에 존재하는 경우에만 해당 key와 value를 matchedContraMap에 저장
                for (Map.Entry<String, List<String>> entry : contraMap.entrySet()) {
                    String otherName = entry.getKey();
                    String reason = "";

                    // Redis에서 note 불러오기
                    String detailKey = "DUR:INTERACT_DETAIL:" + drugName + ":" + otherName;
                    String detailJson = redisTemplate.opsForValue().get(detailKey);

                    if (detailJson != null) {
                        Map<String, String> detail = objectMapper.readValue(detailJson, new TypeReference<>() {
                        });
                        reason = detail.getOrDefault("reason", "");
                    }


                    // 태그 생성
                    tags.add(new DurDetail(
                            "병용금기",
                            reason.isBlank() ?
                                    "(" + drugName + " + " + otherName + ")" :
                                    "(" + drugName + " + " + otherName + "): " + reason,
                            userDrugNames.contains(otherName)
                    ));

                }
            }

            // 2. 임부금기
            String pregKey = "DUR:PREGNANCY:" + drugId;
            String pregJson = redisTemplate.opsForValue().get(pregKey);
            if (pregJson != null) {
                Map<String, String> value = objectMapper.readValue(pregJson, new TypeReference<>() {
                });
                tags.add(new DurDetail(
                        "임부금기",
                        value.get("conditionValue"),
                        user.getUserProfile().isPregnant()
                ));
            }


            // 3. 노인주의
            String elderKey = "DUR:ELDER:" + drugId;
            String elderJson = redisTemplate.opsForValue().get(elderKey);
            if (elderJson != null) {
                Map<String, String> value = objectMapper.readValue(elderJson, new TypeReference<>() {
                });
                tags.add(new DurDetail(
                        "노인주의",
                        value.get("conditionValue"),
                        isElderly
                ));
            }


            // 4. 연령금기
            String ageKey = "DUR:AGE:" + drugId;
            String ageJson = redisTemplate.opsForValue().get(ageKey);
            if (ageJson != null) {
                Map<String, String> value = objectMapper.readValue(ageJson, new TypeReference<>() {
                });
                String raw = value.getOrDefault("conditionValue", "");
                if (isUserInRestrictedAge(user.getUserProfile().getBirthDate(), raw)) {
                    tags.add(new DurDetail(
                            "연령금기",
                            raw,
                            isUserInRestrictedAge(user.getUserProfile().getBirthDate(), raw)
                    ));
                }
            }

            // 효능군 중복주의 검사
            String therKey = "DUR:THERAPEUTIC_DUP:" + drugId;
            String therJson = redisTemplate.opsForValue().get(therKey);
            if (therJson != null) {

                Map<String, String> value = objectMapper.readValue(therJson, new TypeReference<>() {});
                String className = value.getOrDefault("className", "").trim();
                List<Long> sameClassDrugs = classToDrugIdsMap.getOrDefault(className, List.of());

                String category = value.getOrDefault("category", "");
                String remark = value.getOrDefault("remark", "");
                String note = value.getOrDefault("note", "");

                StringBuilder tagBuilder = new StringBuilder();
                tagBuilder.append(className);
                if (!category.isBlank()) tagBuilder.append(" (").append(category).append(")");
                if ((!remark.isBlank() && !remark.equals("없음"))
                        || (!note.isBlank() && !note.equals("없음"))) {
                    tagBuilder.append(" - ");
                    if (!remark.isBlank() && !remark.equals("없음")) tagBuilder.append(remark);
                    if (!note.isBlank() && !note.equals("없음")) {
                        if (!remark.isBlank()) tagBuilder.append(", ");
                        tagBuilder.append(note);
                    }
                }

                tags.add(new DurDetail(
                        "효능군중복주의",
                        tagBuilder.toString(),
                        !sameClassDrugs.isEmpty()
                ));
            }

            DurTagDto dur = new DurTagDto(
                    drug.id(),
                    drug.drugName(),
                    drug.ingredients(),
                    drug.manufacturer(),
                    tags
            );
            result.add(dur);
        }
        return result;
    }


    private boolean isUserInRestrictedAge(LocalDate birthDate, String conditionValue) {
        if (conditionValue == null || birthDate == null || conditionValue.isBlank()) return false;
        LocalDate today = LocalDate.now();

        int age = Period.between(birthDate, today).getYears();
        int ageInMonths = Period.between(birthDate, today).getYears() * 12
                + Period.between(birthDate, today).getMonths();

        String[] parts = conditionValue.split("\\s*,\\s*");

        for (String part : parts) {
            int limit;
            try {
                limit = Integer.parseInt(part.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                continue;
            }

            if (part.contains("개월 미만")) {
                if (ageInMonths < limit) return true;
            } else if (part.contains("개월 이하")) {
                if (ageInMonths <= limit) return true;
            } else if (part.contains("개월 초과")) {
                if (ageInMonths > limit) return true;
            } else if (part.contains("개월 이상")) {
                if (ageInMonths >= limit) return true;
            } else if (part.contains("세 미만")) {
                if (age < limit) return true;
            } else if (part.contains("세 이하")) {
                if (age <= limit) return true;
            } else if (part.contains("세 초과")) {
                if (age > limit) return true;
            } else if (part.contains("세 이상")) {
                if (age >= limit) return true;
            }
        }

        return false;
    }

}
