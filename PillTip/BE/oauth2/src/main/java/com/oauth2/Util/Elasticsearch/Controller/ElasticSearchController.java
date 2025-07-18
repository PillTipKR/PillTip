package com.oauth2.Util.Elasticsearch.Controller;

import com.oauth2.Account.Service.AccountService;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Util.Elasticsearch.Dto.ElasticQuery;
import com.oauth2.Util.Elasticsearch.Dto.ElasticsearchDTO;
import com.oauth2.Util.Elasticsearch.Service.ElasticsearchService;
import com.oauth2.Account.Dto.ApiResponse;
import com.oauth2.User.UserInfo.Entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/autocomplete")
@RequiredArgsConstructor
public class ElasticSearchController {

    @Value("${elastic.autocomplete.index}")
    private String index;

    @Value("${elastic.drug.drug}")
    private String drug;

    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;

    @Value("${elastic.drug.ingredient}")
    private String ingredient;

    //static이면 주입이 안됨!!
    @Value("${elastic.autocomplete.page}")
    private int pageSize;

    private final ElasticsearchService elasticsearchService;
    private final AccountService accountService;

    @GetMapping("/drugs")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getDrugSearch(
            @AuthenticationPrincipal Account account,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        return getApiResponseResponseEntity(user, input, page, drug);
    }

    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getManufacturerSearch(
            @AuthenticationPrincipal Account account,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        return getApiResponseResponseEntity(user, input, page, manufacturer);
    }

    @GetMapping("/ingredients")
    public ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getIngredientSearch(
            @AuthenticationPrincipal Account account,
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId
    ) throws IOException {
        User user = accountService.findUserByProfileId(profileId, account.getId());

        return getApiResponseResponseEntity(user, input, page, ingredient);
    }


    private ResponseEntity<ApiResponse<List<ElasticsearchDTO>>> getApiResponseResponseEntity(
            User user, String input, int page, String field) throws IOException {
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
