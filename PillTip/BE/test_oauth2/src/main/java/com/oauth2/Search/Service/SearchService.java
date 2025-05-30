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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class SearchService {

    @Value("${elastic.allSearch}")
    private String index;

    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchService elasticsearchService;
    private final DrugRepository drugRepository;
    private final IngredientRepository ingredientRepository;
    private final DrugIngredientRepository drugIngredientRepository;

    public SearchService(ElasticsearchClient elasticsearchClient, ElasticsearchService elasticsearchService, DrugRepository drugRepository, IngredientRepository ingredientRepository, DrugIngredientRepository drugIngredientRepository) {
        this.elasticsearchClient = elasticsearchClient;
        this.elasticsearchService = elasticsearchService;
        this.drugRepository = drugRepository;
        this.ingredientRepository = ingredientRepository;
        this.drugIngredientRepository = drugIngredientRepository;
    }

    public void syncDrugsToElasticsearch() throws IOException {
        List<Drug> drugs = drugRepository.findAll();
        for (int i = 0; i < 1000; i++) {
            Drug drug = drugs.get(i);
            List<DrugIngredient> di = drugIngredientRepository.findById_DrugId(drug.getId());
            List<IngredientDetail> ingredientDetails = new ArrayList<>();
            for(DrugIngredient ding : di){
                Optional<Ingredient> ing =
                        ingredientRepository.findById(ding.getId().getIngredientId());
                if(ing.isPresent()){
                    IngredientDetail isidto = new IngredientDetail(
                            ing.get().getNameKr(),
                            ding.getAmountBackup()+ding.getUnit(),
                            false
                    );
                    ingredientDetails.add(isidto);
                }
                System.out.println(ingredientDetails.size());
                ingredientDetails.sort(Collections.reverseOrder());
                ingredientDetails.get(0).setMain(true);
            }
            //for (Drug drug : drugs) {
            SearchIndexDTO dto = new SearchIndexDTO(
                    drug.getId(),
                    drug.getName(),
                    ingredientDetails,
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

    public List<SearchIndexDTO> getDrugSearch(String input, String field,
                                       int pageSize, int page) throws IOException {
        List<String> source = List.of();
        ElasticQuery eq = new ElasticQuery(input,field, index,source,pageSize,page);
        return elasticsearchService.getMatchingFromElasticsearch(eq, SearchIndexDTO.class);

    }

}
