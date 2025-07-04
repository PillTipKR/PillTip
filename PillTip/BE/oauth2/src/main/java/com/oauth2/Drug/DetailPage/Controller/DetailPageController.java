package com.oauth2.Drug.DetailPage.Controller;

import com.oauth2.Drug.DetailPage.Dto.DrugDetail;
import com.oauth2.Drug.DetailPage.Service.DrugDetailService;
import com.oauth2.Drug.DetailPage.Service.DrugPromptService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/detailPage")
@RequiredArgsConstructor
public class DetailPageController {

    private final DrugDetailService drugDetailService;
    private final DrugPromptService drugPromptService;

    @GetMapping
    public ResponseEntity<ApiResponse<DrugDetail>> detailPage(
            @AuthenticationPrincipal User user,
            @RequestParam long id) throws IOException {
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not authenticated", null));
        }

        DrugDetail detail = drugDetailService.getDetail(user, id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @GetMapping("/gpt")
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
}
