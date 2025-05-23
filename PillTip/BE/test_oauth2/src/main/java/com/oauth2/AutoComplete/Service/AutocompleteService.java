package com.oauth2.AutoComplete.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.analysis.*;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch.indices.IndexSettingsAnalysis;
import com.oauth2.AutoComplete.Dto.AutocompleteResult;
import com.oauth2.AutoComplete.Dto.DrugDTO;
import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Repository.DrugRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AutocompleteService {

    private final ElasticsearchClient elasticsearchClient;
    private final RedisService redisService;
    private final DrugRepository drugRepository;

    @Value("${elastic.index}")
    private String index;

    @Value("${elastic.nGramAnalyzer}")
    private String autoNGramAnalyzer;

    @Value("${elastic.edgeNGramAnalyzer}")
    private String autoEdgeNGramAnalyzer;
    @Value("${elastic.nGram}")
    private String nGram;
    @Value("${elastic.edgeNGram}")
    private String edgeNGram;

    @Value("${elastic.drug.id}")
    private String id;

    @Value("${elastic.drug.drug}")
    private String drugName;
    @Value("${elastic.drug.manufacturer}")
    private String manufacturer;

    @Value("${elastic.drug.ingredient}")
    private String ingredient;


    public AutocompleteService(ElasticsearchClient elasticsearchClient, RedisService redisService, DrugRepository drugRepository) {
        this.elasticsearchClient = elasticsearchClient;
        this.redisService = redisService;
        this.drugRepository = drugRepository;
    }

    // 인덱스를 기준으로 analyzer,tokenizer,indexsetting 등을 가짐
    // 즉, 성분, 제조사 등의 DTO가 다르다면, 다른 타입이 매핑되어야 하므로, 새 인덱스를 생성해야할듯
    // 성분, 제조사는 약과 같이 이미지가 없으므로, 다른 dto를 사용할 가능성이 높음 / 혹은 프론트 단에서 무시
    // 이미지는 어떻게 넣을것?
    //- firebase에 약품 id와 매핑해서 저장 후, 약의 대한 정보를 주면 firebase에서 받아오기
    //- 이미지 url도 DB에 저장해서 사용하기

    @PostConstruct //완벽해지면 최초실행시에만 활성화하고 그 뒤로는 꺼두기
    public void createIndex() throws IOException {
        // 먼저 기존 인덱스 삭제 (개발 중일 경우만)
        if (elasticsearchClient.indices().exists(e -> e.index(index)).value()) {
            elasticsearchClient.indices().delete(d -> d.index(index));
        }

        NGramTokenizer nGramTokenizer = new NGramTokenizer.Builder()
                // 단어를 최소 1글자에서 4글자 까지 나눔
                // ex) 에어신신파스 -> 에, 어, 신, 신 ..., 에어, 어신 ..., 에어신 어신파 ..., 에어신신 ..., 신신파스
                .minGram(1)
                .maxGram(4)
                .tokenChars(TokenChar.Letter, TokenChar.Digit, TokenChar.Symbol,TokenChar.Punctuation,TokenChar.Whitespace)
                .build();

        TokenizerDefinition tokenizerNGram = new TokenizerDefinition.Builder()
                .ngram(nGramTokenizer)
                .build();

        CustomAnalyzer customNGramAnalyzer = new CustomAnalyzer.Builder()
                .tokenizer(nGram)
                .filter("lowercase")
                .build();

        Analyzer nGramAnalyzer = new Analyzer.Builder()
                .custom(customNGramAnalyzer)
                .build();

        EdgeNGramTokenizer edgeNGramTokenizer = new EdgeNGramTokenizer.Builder()
                // 단어를 최소 1글자에서 4글자 까지 나눔
                // ex) 에어신신파스 -> 에, 어, 신, 신 ..., 에어, 어신 ..., 에어신 어신파 ..., 에어신신 ..., 신신파스
                .minGram(1)
                .maxGram(20)
                .tokenChars(TokenChar.Letter, TokenChar.Digit, TokenChar.Symbol,TokenChar.Punctuation,TokenChar.Whitespace)
                .build();

        TokenizerDefinition tokenizerEdgeNGram = new TokenizerDefinition.Builder()
                .edgeNgram(edgeNGramTokenizer)
                .build();

        CustomAnalyzer customEdgeNGramAnalyzer = new CustomAnalyzer.Builder()
                .tokenizer(edgeNGram)
                .filter("lowercase")
                .build();

        Analyzer edgeNGramAnalyzer = new Analyzer.Builder()
                .custom(customEdgeNGramAnalyzer)
                .build();


        // 분석기 설정
        IndexSettingsAnalysis analysis = new IndexSettingsAnalysis.Builder()
                .analyzer(autoNGramAnalyzer, nGramAnalyzer)
                .analyzer(autoEdgeNGramAnalyzer, edgeNGramAnalyzer)
                .tokenizer(nGram, b -> b.definition(tokenizerNGram))
                .tokenizer(edgeNGram, b->b.definition(tokenizerEdgeNGram))
                .build();

        IndexSettings settings = new IndexSettings.Builder()
                .maxNgramDiff(5)
                .analysis(analysis)
                .build();

        // Mapping 설정
        TypeMapping mapping = new TypeMapping.Builder()
                .properties(id, p -> p
                        .keyword(k -> k.index(false))
                )
                .properties(drugName, p -> p
                        .text(t -> t.fields("edge", f->f.text(edge -> edge.analyzer(autoEdgeNGramAnalyzer)))
                                .fields("gram", f->f.text(gram->gram.analyzer(autoNGramAnalyzer))))
                )
                .properties(manufacturer, p -> p
                        .text(t -> t.fields("edge", f->f.text(edge -> edge.analyzer(autoEdgeNGramAnalyzer)))
                                .fields("gram", f->f.text(gram->gram.analyzer(autoNGramAnalyzer))))
                )
                .build();

        // 인덱스 생성 요청 빌드
        CreateIndexRequest request = new CreateIndexRequest.Builder()
                .index(index)
                .settings(settings)
                .mappings(mapping)
                .build();

        // 인덱스 생성
        elasticsearchClient.indices().create(request);
        syncDrugsToElasticsearch();
    }

    public void syncDrugsToElasticsearch() throws IOException {
        List<Drug> drugs = drugRepository.findAll();
        for (int i=0; i<1000; i++){
            Drug drug = drugs.get(i);
        //for (Drug drug : drugs) {
            DrugDTO dto = new DrugDTO(
                    drug.getId(),
                    drug.getName(),
                    drug.getManufacturer(),
                    null // imageUrl은 필요시 Redis 또는 별도 처리
            );

            IndexRequest<DrugDTO> indexRequest = new IndexRequest.Builder<DrugDTO>()
                    .index(index)
                    .id(String.valueOf(dto.id()))
                    .document(dto)
                    .build();

            elasticsearchClient.index(indexRequest);
        }
    }

    public List<AutocompleteResult> getDrugSearch(String input, String field,
                                                  int pageSize, int page) throws IOException {
        List<AutocompleteResult> result = new ArrayList<>();

        List<DrugDTO> drugs = getMatchingDrugsFromElasticsearch(input, field, pageSize, page);
        for (DrugDTO drug : drugs) {
            String drugId = String.valueOf(drug.id());
            String drugName = redisService.getDrugNameFromCache(drugId);
            String manufacturer = redisService.getManufacturerFromCache(drugId);
            String imageUrl = redisService.getImageUrlFromCache(drugId);

            result.add(new AutocompleteResult(
                    drug.id(),
                    drugName != null ? drugName : drug.drugName(),
                    manufacturer != null? manufacturer : drug.manufacturer(),
                    imageUrl != null ? imageUrl : drug.imageUrl()
            ));
        }
        return result;
    }

    private List<DrugDTO> getMatchingDrugsFromElasticsearch(String input, String field,
                                                            int pageSize, int page) throws IOException {
        int from = page * pageSize;

        SearchResponse<DrugDTO> response = elasticsearchClient.search(s -> s
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
                , DrugDTO.class

        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }
}