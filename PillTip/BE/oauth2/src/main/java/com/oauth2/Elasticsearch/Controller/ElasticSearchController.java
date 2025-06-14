package com.oauth2.Elasticsearch.Controller;

import com.oauth2.Elasticsearch.Dto.ElasticQuery;
import com.oauth2.Elasticsearch.Dto.ElasticsearchDTO;
import com.oauth2.Elasticsearch.Service.ElasticsearchService;
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
@RequestMapping("/api/autocomplete")
public class ElasticSearchController {

    @Value("${elastic.autocomplete.index}")
    private String index;

    @Value("${elastic.drug.drug}")
    private String drug;

    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;
    @Value("${elastic.drug.ingredient.index}")
    private String ingredient;

    //static이면 주입이 안됨!!
    @Value("${elastic.autocomplete.page}")
    private int pageSize;

    private final ElasticsearchService elasticsearchService;

    public ElasticSearchController(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @GetMapping("/drugs")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getDrugSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return getApiResponseResponseEntity(user, input, page, drug);
    }

    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getManufacturerSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return getApiResponseResponseEntity(user, input, page, manufacturer);
    }

    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getIngredientSearch(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return getApiResponseResponseEntity(user, input, page, ingredient);
    }


    private ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getApiResponseResponseEntity(
            @AuthenticationPrincipal User user,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page, String field) throws IOException {
        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated", null));
        }
        List<String> filter = List.of();
        ElasticQuery eq = new ElasticQuery(input, field, index, filter, pageSize, page);
        List<ElasticsearchDTO> result = elasticsearchService.getMatchingFromElasticsearch(eq, ElasticsearchDTO.class);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

}
