package com.oauth2.Search.Controller;

import com.oauth2.DUR.Dto.DurTagDto;
import com.oauth2.DUR.Service.DurTaggingService;
import com.oauth2.Search.Dto.SearchIndexDTO;
import com.oauth2.Search.Service.SearchService;
import com.oauth2.User.dto.ApiResponse;
import com.oauth2.User.entity.User;
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
    public ResponseEntity<ApiResponse<List<DurTagDto>>> getDrugSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return getTagSearch(user,input,page,drug);
    }

    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse<List<DurTagDto>>> getManufacturerSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return getTagSearch(user,input,page,manufacturer);
    }

    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<DurTagDto>>> getIngredientSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue="0") int page) throws IOException {
        return getTagSearch(user,input,page,ingredient);
    }

    private ResponseEntity<ApiResponse<List<DurTagDto>>> getTagSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page, String field) throws IOException {
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated", null));
        }
        List<SearchIndexDTO> searchIndexDTOList = searchService.getDrugSearch(input, field, pageSize, page);
         List<DurTagDto> result = durTaggingService.generateTagsForDrugs(
                user, searchIndexDTOList);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
