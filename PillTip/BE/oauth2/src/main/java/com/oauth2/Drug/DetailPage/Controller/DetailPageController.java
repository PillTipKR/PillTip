package com.oauth2.Drug.DetailPage.Controller;

import com.oauth2.Drug.DetailPage.Dto.DrugDetail;
import com.oauth2.Drug.DetailPage.Service.DrugDetailService;
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

}
