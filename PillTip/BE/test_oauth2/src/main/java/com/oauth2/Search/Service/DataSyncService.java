package com.oauth2.Search.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Domain.DrugIngredient;
import com.oauth2.Drug.Domain.Ingredient;
import com.oauth2.Drug.Repository.DrugIngredientRepository;
import com.oauth2.Drug.Repository.DrugRepository;
import com.oauth2.Drug.Repository.IngredientRepository;
import com.oauth2.Elasticsearch.Dto.ElasticsearchDTO;
import com.oauth2.Search.Dto.IngredientDetail;
import com.oauth2.Search.Dto.SearchIndexDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataSyncService {


    @Value("${redis.drug.drug}")
    private String hashDrug;
    @Value("${redis.drug.manufacturer}")
    private String hashMaufacturer;
    @Value("${elastic.autocomplete.index}")
    private String autocomplete;
    @Value("${elastic.allSearch}")
    private String search;

    @Value("${elastic.drug.drug}")
    private String drugName;

    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;

    @Value("${elastic.drug.ingredient}")
    private String ingredientName;


    private final RedisService redisService;
    private final DrugRepository drugRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final IngredientRepository ingredientRepository;
    private final DrugIngredientRepository drugIngredientRepository;

    private void syncDataWithRedis() {
        List<Drug> drugs = drugRepository.findAll();
        for (Drug drug : drugs) {
            String drugId = String.valueOf(drug.getId());
            redisService.addAutocompleteSuggestion(drugId, hashDrug,drug.getName());
            redisService.addAutocompleteSuggestion(drugId, hashMaufacturer, drug.getManufacturer());
        }
    }

    private void syncTextToElasticsearch() throws IOException {
        List<Drug> drugs = drugRepository.findAll();
        List<Ingredient> ingredients = ingredientRepository.findAll();
        Set<String> manufacturers = new HashSet<>();
        for (Drug drug : drugs){
            injectIndex(drugName,drug.getName());
            if (manufacturers.add(drug.getManufacturer())) {
                injectIndex(manufacturer, drug.getManufacturer());
            }
        }
        for(Ingredient ingredient : ingredients){
            injectIndex(ingredientName,ingredient.getNameKr());
        }
        System.out.println("index injection completed");
    }

    private void injectIndex(String type, String value) throws IOException {
        // 중복 방지를 위한 고유 ID 생성
        ElasticsearchDTO elasticsearchDTO = new ElasticsearchDTO(
                type,
                value
        );

        IndexRequest<ElasticsearchDTO> indexRequest = new IndexRequest.Builder<ElasticsearchDTO>()
                .index(autocomplete)
                .document(elasticsearchDTO)
                .build();

        elasticsearchClient.index(indexRequest);
    }


    private void syncDrugsToElasticsearch() throws IOException {
        List<Drug> drugs = drugRepository.findAll();
        for (Drug drug : drugs) {
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
                ingredientDetails.sort(Collections.reverseOrder());
                ingredientDetails.get(0).setMain(true);
            }
            SearchIndexDTO dto = new SearchIndexDTO(
                    drug.getId(),
                    drug.getName(),
                    ingredientDetails,
                    drug.getManufacturer()
            );

            IndexRequest<SearchIndexDTO> indexRequest = new IndexRequest.Builder<SearchIndexDTO>()
                    .index(search)
                    .id(String.valueOf(dto.id()))
                    .document(dto)
                    .build();

            elasticsearchClient.index(indexRequest);
        }
    }

    public void loadAll() throws IOException {
        syncDrugsToElasticsearch();
        syncDataWithRedis();
        syncTextToElasticsearch();
    }
}
