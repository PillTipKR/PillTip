package com.oauth2.Search.Controller;

import com.oauth2.Search.Dto.DrugDTO;
import com.oauth2.Search.Dto.SearchIndexDTO;
import com.oauth2.Search.Service.SearchService;
import org.springframework.beans.factory.annotation.Value;
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
    public List<SearchIndexDTO> getDrugSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return searchService.getDrugSearch(input, drug, pageSize, page);

/*
        List<String> filter = List.of(id,drug);
        ElasticQuery eq = ElasitcQueryBuilder.queryBuild(input, drug, filter,page);
        return elasticsearchService.getMatchingFromElasticsearch(eq, ElasticsearchDTO.class);
 */
    }

    @GetMapping("/manufacturers")
    public List<SearchIndexDTO> getManufacturerSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        return searchService.getDrugSearch(input, manufacturer, pageSize, page);
    }

    @GetMapping("/ingredients")
    public List<SearchIndexDTO> getIngredientSearch(
            @RequestParam String input,
            @RequestParam(defaultValue="0") int page) throws IOException{
        return searchService.getDrugSearch(input, ingredient,pageSize, page);
    }

}
