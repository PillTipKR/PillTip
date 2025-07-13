package com.oauth2.Drug.Prompt.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.Drug.DUR.Service.DurService;
import com.oauth2.Drug.DetailPage.Dto.DrugDetail;
import com.oauth2.Drug.Prompt.Dto.DurResponse;
import com.oauth2.Drug.Prompt.Service.DrugPromptService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PromptController {

    private final DrugPromptService drugPromptService;
    private final DurService durService;

    @PostMapping("/detailPage/gpt")
    public ResponseEntity<ApiResponse<String>> askGPT(
            @AuthenticationPrincipal User user,
            @RequestBody DrugDetail detail) {
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated", null));
        }

        String gptExplain = drugPromptService.getAsk(user, detail);
        return ResponseEntity.ok(ApiResponse.success(gptExplain));
    }


    @GetMapping("/dur/gpt")
    public ResponseEntity<ApiResponse<DurResponse>> analysisDur(
            @AuthenticationPrincipal User user,
            @RequestParam long drugId1,
            @RequestParam long drugId2) throws JsonProcessingException {
        // 복약 완료 처리 로직
        DurResponse response =
                drugPromptService.askDur(durService.generateTagsForDrugs(user, drugId1, drugId2));
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }


}
