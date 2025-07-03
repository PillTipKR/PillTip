package com.oauth2.DetailPage.Service;

import com.oauth2.DetailPage.Dto.*;
import com.oauth2.User.dto.TakingPillSummaryResponse;
import com.oauth2.User.entity.User;
import com.oauth2.User.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DrugPromptService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${api.url}")
    private String API_URL;

    @Value("${openai.model}")
    private String model;

    private final UserProfileService userProfileService;


    private PromptRequestDto buildPromptRequestDto(User user, DrugDetail detail) {
        List<String> medicationNames = userProfileService.getTakingPillSummary(user).getTakingPills().stream()
                .map(TakingPillSummaryResponse.TakingPillSummary::getMedicationName)
                .toList();

        return new PromptRequestDto(
                detail.durTags(),
                user.getUserProfile().getAge(),
                user.getUserProfile().getGender().name(),
                user.getUserProfile().getDiseaseInfo(),
                medicationNames,
                new DrugRequestInfoDto(
                        detail.name(),
                        detail.effect(),
                        detail.usage(),
                        detail.caution()
                )
        );
    }

    public String getAsk(User user, DrugDetail detail) {
        String prompt = buildPrompt(buildPromptRequestDto(user, detail));
        return askGPT(prompt);
    }

    private String askGPT(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        // 메시지 구성
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        GPTRequest request = new GPTRequest(
                model,
                Collections.singletonList(userMessage),
                0.3,
                1000

        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<GPTRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<GPTResponse> response = restTemplate.postForEntity(API_URL, entity, GPTResponse.class);

        assert response.getBody() != null;
        return response.getBody().getChoices().get(0).getMessage().getContent();
    }

    private String buildPrompt(PromptRequestDto dto) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 사용자의 상황과 복약정보에 따라 맞춤형 약물 소개를 하는 역할입니다.\n\n");

        sb.append("- DUR 정보가 존재하는 경우\n")
                .append("**해요체**로 부드럽게 경고합니다. (예: \"이 약은 임신 중에는 복용하면 안 돼요.\")\n\n")
                .append("- DUR 정보가 비어 있는 경우**\n")
                .append("**해요체**로 효능과 해당 사용자 정보와 관련된 주의사항만 간단히 알려줍니다. \n")
                .append("사용자 정보 기반 조건 매칭이 우선, 그 외의 주의는 일반적 상황일 때만 언급\n\n")
                .append("**마크다운을 사용하지 마세요**")
                .append("**문맥이 자연스럽게 흘러가도록 가독성을 높여서 작성해주세요**\n")
                .append("**반드시 출력은 280자 ~ 300자 사이로 유지해주세요.**\n\n");

        // DUR 정보
        sb.append("DUR 정보: ");
        sb.append(dto.durInfo() == null || dto.durInfo().isEmpty() ? "\"\"" : dto.durInfo());
        sb.append("\n");

        // 사용자 정보
        sb.append("사용자 정보: { ")
                .append(dto.age()).append(", ")
                .append(dto.gender()).append(", ")
                .append(dto.underlyingDisease().isEmpty() ? "\"\"" : dto.underlyingDisease()).append(", {");

        if (dto.currentDrugs() != null && !dto.currentDrugs().isEmpty()) {
            String drugList = dto.currentDrugs().stream()
                    .map(d -> "\"" + d + "\"")
                    .collect(Collectors.joining(", "));
            sb.append(drugList);
        }
        sb.append("} } \n");

        // 약 정보
        sb.append("약 정보: { ").append(dto.drugInfo()).append(" }\n\n");

        // 출력 포맷 안내
        sb.append("출력 형식:\n[사용자 맞춤형 안내. 반드시 해요체. 반드시 280~300자]")
            .append("출력 시 반드시 다음 요소를 포함해야 해요:\n")
            .append("1. 약의 **상세한 기능 또는 작용 기전** (예: 혈압을 낮춰요, 근육통을 줄여줘요 등\n")
            .append("2. **사용자 조건**과 **위험 내용**을 명확히 연결해서 설명 (예: 노인은 어지럼증 위험이 커요)");

        return sb.toString();
    }

}
