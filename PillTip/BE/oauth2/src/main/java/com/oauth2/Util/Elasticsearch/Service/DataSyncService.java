package com.oauth2.Util.Elasticsearch.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.oauth2.Drug.DrugInfo.Domain.Drug;
import com.oauth2.Drug.DrugInfo.Domain.DrugIngredient;
import com.oauth2.Drug.DrugInfo.Domain.Ingredient;
import com.oauth2.Drug.DrugInfo.Repository.DrugIngredientRepository;
import com.oauth2.Drug.DrugInfo.Repository.DrugRepository;
import com.oauth2.Drug.DrugInfo.Repository.IngredientRepository;
import com.oauth2.Drug.Search.Dto.IngredientComp;
import com.oauth2.Drug.Search.Dto.IngredientDetail;
import com.oauth2.Drug.Search.Dto.SearchIndexDTO;
import com.oauth2.Util.Elasticsearch.Dto.ElasticsearchDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataSyncService {

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


    private final DrugRepository drugRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final IngredientRepository ingredientRepository;
    private final DrugIngredientRepository pillIngredientRepository;

    private void syncTextToElasticsearch() throws IOException {
        List<Drug> pills = drugRepository.findAll();
        List<Ingredient> ingredients = ingredientRepository.findAll();
        Set<String> manufacturers = new HashSet<>();
        for (Drug pill : pills){
            injectIndex(drugName, pill.getId(), pill.getName(), pill.getImage());
            if (manufacturers.add(pill.getManufacturer())) {
                injectIndex(manufacturer, 0L, pill.getManufacturer(), null);
            }
        }
        for(Ingredient ingredient : ingredients){
            injectIndex(ingredientName,ingredient.getId(),ingredient.getNameKr(),null);
        }
        System.out.println("index injection completed");
    }

    private void injectIndex(String type,Long id, String value, String imageUrl) throws IOException {
        // 중복 방지를 위한 고유 ID 생성
        ElasticsearchDTO elasticsearchDTO = new ElasticsearchDTO(
                type,
                id,
                value,
                imageUrl
        );

        IndexRequest<ElasticsearchDTO> indexRequest = new IndexRequest.Builder<ElasticsearchDTO>()
                .index(autocomplete)
                .document(elasticsearchDTO)
                .build();

        elasticsearchClient.index(indexRequest);
    }


    private void syncDrugsToElasticsearch() throws IOException {
        List<Drug> pills = drugRepository.findAll();
        for (Drug pill : pills) {
            List<DrugIngredient> di = pillIngredientRepository.findById_DrugId(pill.getId());
            List<IngredientComp> ingredientComps = new ArrayList<>();
            for(DrugIngredient ding : di){
                Optional<Ingredient> ing =
                        ingredientRepository.findById(ding.getId().getIngredientId());
                if(ing.isPresent()){
                    IngredientComp isidto = new IngredientComp(
                            ing.get().getNameKr(),
                            ding.getAmount() !=null? ding.getAmount():0,
                            ding.getAmountBackup()+ding.getUnit(),
                            false
                    );
                    ingredientComps.add(isidto);
                }
            }
            if(!ingredientComps.isEmpty()) {
                ingredientComps.sort(Collections.reverseOrder());
                ingredientComps.get(0).setMain(true);
                SearchIndexDTO dto = getSearchIndexDTO(pill, ingredientComps);

                IndexRequest<SearchIndexDTO> indexRequest = new IndexRequest.Builder<SearchIndexDTO>()
                        .index(search)
                        .id(String.valueOf(dto.id()))
                        .document(dto)
                        .build();

                elasticsearchClient.index(indexRequest);
            }
        }
    }

    private static SearchIndexDTO getSearchIndexDTO(Drug pill, List<IngredientComp> ingredientComps) {
        List<IngredientDetail> ingredientDetails = new ArrayList<>();
        for(IngredientComp ingredientComp : ingredientComps){
            IngredientDetail ingredientDetail = new IngredientDetail(
                    ingredientComp.getName(),
                    ingredientComp.getBackup(),
                    ingredientComp.isMain());
            ingredientDetails.add(ingredientDetail);
        }
        return new SearchIndexDTO(
                pill.getId(),
                pill.getName(),
                ingredientDetails,
                pill.getManufacturer()
        );
    }

    public void loadAll() throws IOException {
        syncDrugsToElasticsearch();
        syncTextToElasticsearch();
    }
}
