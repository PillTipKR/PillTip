package com.oauth2.Drug.Search.Controller;

import com.oauth2.Drug.DUR.Dto.SearchDurDto;
import com.oauth2.Drug.DUR.Service.DurTaggingService;
import com.oauth2.Drug.Search.Dto.SearchIndexDTO;
import com.oauth2.Drug.Search.Service.SearchService;
import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Auth.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    @Value("${elastic.drug.drug}")
    private String drug;

    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;
    @Value("${elastic.drug.ingredient}")
    private String ingredient;
    @Value("${elastic.page}")
    private int pageSize;

    private final SearchService searchService;
    private final DurTaggingService durTaggingService;

    @GetMapping("/drugs")
    public ResponseEntity<ApiResponse<List<SearchDurDto>>> getDrugSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return getTagSearch(user,input,page,drug);
    }

    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse<List<SearchDurDto>>> getManufacturerSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return getTagSearch(user,input,page,manufacturer);
    }

    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<SearchDurDto>>> getIngredientSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue="0") int page) throws IOException {
        return getTagSearch(user,input,page,ingredient);
    }

    private ResponseEntity<ApiResponse<List<SearchDurDto>>> getTagSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page, String field) throws IOException {
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated", null));
        }
        List<SearchIndexDTO> searchIndexDTOList = searchService.getDrugSearch(input, field, pageSize, page);
         List<SearchDurDto> result = durTaggingService.generateTagsForDrugs(
                user, searchIndexDTOList);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
