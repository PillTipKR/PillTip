package com.oauth2.Search.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Repository.DrugRepository;
import com.oauth2.Drug.Repository.IngredientRepository;
import com.oauth2.Search.Dto.DrugDTO;
import com.oauth2.Search.Dto.SearchIndexDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class SearchService {

    @Value("${elastic.search}")
    private String index;

    private final ElasticsearchClient elasticsearchClient;
    private final RedisService redisService;
    private final DrugRepository drugRepository;

    private final IngredientRepository ingredientRepository;

    public SearchService(ElasticsearchClient elasticsearchClient, RedisService redisService, DrugRepository drugRepository, IngredientRepository ingredientRepository) {
        this.elasticsearchClient = elasticsearchClient;
        this.redisService = redisService;
        this.drugRepository = drugRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public void syncDrugsToElasticsearch() throws IOException {
        List<Drug> drugs = drugRepository.findAll();
        for (int i = 0; i < 1000; i++) {
            Drug drug = drugs.get(i);
            //for (Drug drug : drugs) {
            SearchIndexDTO dto = new SearchIndexDTO(
                    drug.getId(),
                    drug.getName(),
                    drug.getManufacturer()
            );

            IndexRequest<SearchIndexDTO> indexRequest = new IndexRequest.Builder<SearchIndexDTO>()
                    .index(index)
                    .id(String.valueOf(dto.id()))
                    .document(dto)
                    .build();

            elasticsearchClient.index(indexRequest);
        }
    }

    public List<DrugDTO> getDrugSearch(String input, String field,
                                       int pageSize, int page) throws IOException {
        List<DrugDTO> result = new ArrayList<>();

        List<SearchIndexDTO> drugs = getMatchingDrugsFromElasticsearch(input, field, pageSize, page);
        for (SearchIndexDTO drug : drugs) {
            String drugId = String.valueOf(drug.id());
            String drugName = redisService.getDrugNameFromCache(drugId);
            String manufacturer = redisService.getManufacturerFromCache(drugId);
            String imageUrl = redisService.getImageUrlFromCache(drugId);

            result.add(new DrugDTO(
                    drug.id(),
                    drugName != null ? drugName : drug.drugName(),
                    manufacturer != null? manufacturer : drug.manufacturer(),
                    imageUrl = imageUrl
            ));
        }
        return result;
    }

    private List<SearchIndexDTO> getMatchingDrugsFromElasticsearch(String input, String field,
                                                            int pageSize, int page) throws IOException {
        int from = page * pageSize;

        SearchResponse<SearchIndexDTO> response = elasticsearchClient.search(s -> s
                        .index(index)
                        .from(from)
                        .size(pageSize)
                        .query(q -> q
                                .bool(b -> b
                                        .minimumShouldMatch("1")
                                        .should(sh -> sh.prefix(p -> p
                                                .field(field + ".edge")
                                                .value(input)
                                                .boost(90.0f)
                                        )).should(sh -> sh.match(p -> p
                                                .field(field + ".gram")
                                                .query(input)
                                                .boost(5.0f)
                                        ))
                                        .should(sh -> sh.fuzzy(f -> f
                                                .field(field)
                                                .value(input)
                                                .fuzziness("AUTO")
                                                .boost(1.0f)
                                        ))
                                )
                        )
                , SearchIndexDTO.class

        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

}
