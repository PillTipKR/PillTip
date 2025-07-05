package com.oauth2.Drug.DUR.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.oauth2.Drug.DUR.Dto.DurAnalysisResponse;
import com.oauth2.Drug.DUR.Service.DurService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DurController {

    private final DurService durService;
    private final Logger logger = LoggerFactory.getLogger(DurImportController.class);

    @GetMapping("/dur")
    public ResponseEntity<ApiResponse<DurAnalysisResponse>> analysisDur(
            @AuthenticationPrincipal User user,
            @RequestParam long drugId1,
            @RequestParam long drugId2) throws JsonProcessingException {
        // 복약 완료 처리 로직
        DurAnalysisResponse durAnalysisResponse = durService.generateTagsForDrugs(user, drugId1, drugId2);
        return ResponseEntity.ok().body(ApiResponse.success(durAnalysisResponse));
    }
}
