package com.oauth2.AutoComplete.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.oauth2.AutoComplete.Dto.AutocompleteDTO;
import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Domain.Ingredient;
import com.oauth2.Drug.Repository.DrugRepository;
import com.oauth2.Drug.Repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AutocompleteService {


    @Value("${elastic.autocomplete}")
    private String index;

    private final ElasticsearchClient elasticsearchClient;
    private final DrugRepository drugRepository;

    private final IngredientRepository ingredientRepository;

    public AutocompleteService(ElasticsearchClient elasticsearchClient, DrugRepository drugRepository, IngredientRepository ingredientRepository) {
        this.elasticsearchClient = elasticsearchClient;
        this.drugRepository = drugRepository;
        this.ingredientRepository = ingredientRepository;
    }

    // 인덱스를 기준으로 analyzer,tokenizer,indexsetting 등을 가짐
    // 즉, 성분, 제조사 등의 DTO가 다르다면, 다른 타입이 매핑되어야 하므로, 새 인덱스를 생성해야할듯
    // 성분, 제조사는 약과 같이 이미지가 없으므로, 다른 dto를 사용할 가능성이 높음 / 혹은 프론트 단에서 무시
    // 이미지는 어떻게 넣을것?
    //- firebase에 약품 id와 매핑해서 저장 후, 약의 대한 정보를 주면 firebase에서 받아오기
    //- 이미지 url도 DB에 저장해서 사용하기


    public void syncTextToElasticsearch() throws IOException {
        List<Drug> drugs = drugRepository.findAll();
        List<Ingredient> ingredients = ingredientRepository.findAll();
        Set<String> manufacturers = new HashSet<>();
        for (int i=0; i<1000; i++){
            Drug drug = drugs.get(i);
            injectIndex("drug",drug.getName());
            if (manufacturers.add(drug.getManufacturer())) {
                injectIndex("manufacturer", drug.getManufacturer());
            }
        }
        for(int i=0; i<1000; i++){
            Ingredient ingredient = ingredients.get(i);
            injectIndex("ingredient",ingredient.getNameKr());
        }
        System.out.println("index injection completed");
    }

    private void injectIndex(String type, String value) throws IOException {
        // 중복 방지를 위한 고유 ID 생성
        AutocompleteDTO autocompleteDTO = new AutocompleteDTO(
                type,
                value
        );

        IndexRequest<AutocompleteDTO> indexRequest = new IndexRequest.Builder<AutocompleteDTO>()
                .index(index)
                .document(autocompleteDTO)
                .build();

        elasticsearchClient.index(indexRequest);
    }

    public List<AutocompleteDTO> getAutocompleteFromElasticsearch(String input, String type,
                                                                    int pageSize, int page) throws IOException {
        int from = page * pageSize;

        SearchResponse<AutocompleteDTO> response = elasticsearchClient.search(s -> s
                        .index(index)
                        .from(from)
                        .size(pageSize)
                        .query(q -> q
                                .bool(b -> b
                                        .must(m -> m.term(t -> t.field("type").value(type)))
                                        .minimumShouldMatch("1")
                                        .should(sh -> sh.prefix(p -> p
                                                .field("value.edge")
                                                .value(input)
                                                .boost(90.0f)
                                        )).should(sh -> sh.match(p -> p
                                                .field("value.gram")
                                                .query(input)
                                                .boost(5.0f)
                                        ))
                                        .should(sh -> sh.fuzzy(f -> f
                                                .field("value")
                                                .value(input)
                                                .fuzziness("AUTO")
                                                .boost(1.0f)
                                        ))
                                )
                        )
                , AutocompleteDTO.class

        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }
}