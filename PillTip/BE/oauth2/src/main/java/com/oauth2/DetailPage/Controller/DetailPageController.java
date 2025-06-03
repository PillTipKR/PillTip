package com.oauth2.DetailPage.Controller;

import com.oauth2.DetailPage.Dto.DrugDetail;
import com.oauth2.DetailPage.Service.DrugDetailService;
import com.oauth2.Search.Dto.SearchIndexDTO;
import com.oauth2.User.dto.ApiResponse;
import com.oauth2.User.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/detailPage")
public class DetailPageController {

    private final DrugDetailService drugDetailService;

    public DetailPageController(DrugDetailService drugDetailService) {
        this.drugDetailService = drugDetailService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DrugDetail>> detailPage(
            @AuthenticationPrincipal User user,
            @RequestParam long id) throws IOException {
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not authenticated"));
        }
        
        SearchIndexDTO searchIndexDTO = drugDetailService.getDetailFromElasticsearch(id);
        DrugDetail detail = drugDetailService.getDetail(searchIndexDTO);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }
}
