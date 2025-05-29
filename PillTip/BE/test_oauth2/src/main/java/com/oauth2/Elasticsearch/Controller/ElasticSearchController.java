package com.oauth2.Elasticsearch.Controller;

import com.oauth2.Elasticsearch.Dto.ElasticQuery;
import com.oauth2.Elasticsearch.Dto.ElasticsearchDTO;
import com.oauth2.Elasticsearch.Service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${elastic.drug.ingredient}")
    private String ingredient;

    //static이면 주입이 안됨!!
    @Value("${elastic.autocomplete.page}")
    private int pageSize;

    private final ElasticsearchService elasticsearchService;

    public ElasticSearchController(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @GetMapping("/drugs")
    public List<ElasticsearchDTO> getDrugSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        List<String> filter = List.of();
        ElasticQuery eq = new ElasticQuery(input, drug, index,filter,pageSize,page);
        return elasticsearchService.getMatchingFromElasticsearch(eq, ElasticsearchDTO.class);
    }

    @GetMapping("/manufacturers")
    public List<ElasticsearchDTO> getManufacturerSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        List<String> filter = List.of();
        ElasticQuery eq = new ElasticQuery(input, manufacturer,index,filter,pageSize,page);
        return elasticsearchService.getMatchingFromElasticsearch(eq, ElasticsearchDTO.class);
    }

    @GetMapping("/ingredients")
    public List<ElasticsearchDTO> getIngredientSearch(
            @RequestParam String input,
            @RequestParam(defaultValue = "0") int page) throws IOException {
        List<String> filter = List.of();
        ElasticQuery eq = new ElasticQuery(input, ingredient,index,filter,pageSize,page);
        return elasticsearchService.getMatchingFromElasticsearch(eq, ElasticsearchDTO.class);
    }
}
