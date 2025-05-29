package com.oauth2.Elasticsearch.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.oauth2.Elasticsearch.Dto.ElasticQuery;
import com.oauth2.Elasticsearch.Dto.ElasticsearchDTO;
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
public class ElasticsearchService {


    @Value("${elastic.autocomplete.index}")
    private String autocomplete;

    @Value("${elastic.autocomplete.field}")
    private String autocompleteField;

    @Value("${elastic.drug.drug}")
    private String drugName;

    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;

    @Value("${elastic.drug.ingredient}")
    private String ingredientName;

    private final ElasticsearchClient elasticsearchClient;
    private final DrugRepository drugRepository;

    private final IngredientRepository ingredientRepository;

    public ElasticsearchService(ElasticsearchClient elasticsearchClient, DrugRepository drugRepository, IngredientRepository ingredientRepository) {
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
            injectIndex(drugName,drug.getName());
            if (manufacturers.add(drug.getManufacturer())) {
                injectIndex(manufacturer, drug.getManufacturer());
            }
        }
        for(int i=0; i<1000; i++){
            Ingredient ingredient = ingredients.get(i);
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

    private NestedQuery nestedQuery(String field, String value) {
        String nestedPath = field.split("\\.")[0]; // e.g., "ingredient"
        return NestedQuery.of(q -> q
                .path(nestedPath)
                .query(nq -> nq
                        .bool(b -> b
                                .must(mq -> mq
                                        .match(m -> m
                                                .field(field)
                                                .query(value)
                                        )
                                )
                        )
                )
        );
    }



    public  <T> List<T> getMatchingFromElasticsearch(ElasticQuery eq, Class<T> dtoClass) throws IOException {
        String input = eq.getInput();
        final String[] field = {eq.getField()};
        String index = eq.getIndex();
        int pageSize = eq.getPageSize();
        int from = eq.getPage() * pageSize;
        SearchRequest searchRequest = SearchRequest.of(s -> {
            s = s.index(index)
                    .from(from)
                    .size(pageSize);

            if (eq.getSources() != null) {
                if (!eq.getSources().isEmpty()) {
                    s = s.source(src -> src.filter(f -> f.includes(eq.getSources())));
                } else {
                    s = s.source(src -> src.filter(f -> f.includes("*"))); // or 생략
                }
            }

            s = s.query(q -> q
                    .bool(b -> {
                            if(index.equals(autocomplete)) {
                                System.out.println(field[0]);
                                b = b.must(m -> m.term(t -> t.field("type").value(field[0])));
                                field[0] = autocompleteField;
                                System.out.println(field[0]);
                            }

                        b.minimumShouldMatch("1");

                        if (field[0].startsWith("ingredient.")) {
                                // ingredient.name.edge 등과 같이 nested 하위 필드면 nested 쿼리 삽입
                                b = b.should(sh -> sh.nested(nestedQuery(field[0], input)));
                            } else {
                                        b=b.should(sh -> sh.prefix(p -> p
                                        .field(field[0] + ".edge")
                                        .value(input)
                                        .boost(90.0f)
                                ))
                                        .should(sh -> sh.match(p -> p
                                                .field(field[0] + ".gram")
                                                .query(input)
                                                .boost(5.0f)
                                        ))
                                        .should(sh -> sh.fuzzy(f -> f
                                                        .field(field[0])
                                                        .value(input)
                                                        .fuzziness("AUTO")
                                                        .boost(1.0f)
                                                )
                                        );
                            }
                                    return b;
                            }
                    )
            );
            return s;
        });
        SearchResponse<T> response = elasticsearchClient.search(searchRequest,dtoClass);
        System.out.println(searchRequest.toString());
        System.out.println(response.hits());
        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }
}