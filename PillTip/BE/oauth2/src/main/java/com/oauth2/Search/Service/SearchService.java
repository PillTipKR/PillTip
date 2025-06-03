package com.oauth2.Search.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.oauth2.Drug.Domain.DrugIngredient;
import com.oauth2.Drug.Domain.Ingredient;
import com.oauth2.Elasticsearch.Dto.ElasticQuery;
import com.oauth2.Elasticsearch.Service.ElasticsearchService;
import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Repository.DrugIngredientRepository;
import com.oauth2.Drug.Repository.DrugRepository;
import com.oauth2.Drug.Repository.IngredientRepository;
import com.oauth2.Search.Dto.IngredientDetail;
import com.oauth2.Search.Dto.SearchIndexDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchService {

    @Value("${elastic.allSearch}")
    private String index;

    private final ElasticsearchService elasticsearchService;

    public List<SearchIndexDTO> getDrugSearch(String input, String field,
                                       int pageSize, int page) throws IOException {
        List<String> source = List.of();
        ElasticQuery eq = new ElasticQuery(input,field, index,source,pageSize,page);
        return elasticsearchService.getMatchingFromElasticsearch(eq, SearchIndexDTO.class);

    }

}
