package com.oauth2.Search.Controller;

import com.oauth2.Search.Dto.DrugDTO;
import com.oauth2.Search.Dto.SearchIndexDTO;
import com.oauth2.Search.Service.SearchService;
import com.oauth2.User.dto.ApiResponse;
import com.oauth2.User.entity.User;
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

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/drugs")
    public ResponseEntity<ApiResponse<List<SearchIndexDTO>>> getDrugSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not authenticated", null));
        }
        List<SearchIndexDTO> result = searchService.getDrugSearch(input, drug, pageSize, page);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse<List<SearchIndexDTO>>> getManufacturerSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not authenticated", null));
        }
        List<SearchIndexDTO> result = searchService.getDrugSearch(input, manufacturer, pageSize, page);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<SearchIndexDTO>>> getIngredientSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue="0") int page) throws IOException {
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User not authenticated", null));
        }
        List<SearchIndexDTO> result = searchService.getDrugSearch(input, ingredient, pageSize, page);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
