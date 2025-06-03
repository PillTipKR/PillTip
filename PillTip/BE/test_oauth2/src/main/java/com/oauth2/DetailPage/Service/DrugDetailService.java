package com.oauth2.DetailPage.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.oauth2.DetailPage.Dto.DrugDetail;
import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Domain.DrugEffect;
import com.oauth2.Drug.Domain.DrugStorageCondition;
import com.oauth2.Drug.Repository.DrugEffectRepository;
import com.oauth2.Drug.Repository.DrugRepository;
import com.oauth2.Drug.Repository.DrugStorageConditionRepository;
import com.oauth2.Search.Dto.SearchIndexDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DrugDetailService {

    private final DrugRepository drugRepository;
    private final DrugEffectRepository drugEffectRepository;
    private final DrugStorageConditionRepository conditionRepository;
    private final ElasticsearchClient elasticsearchClient;

    @Value("${elastic.drug.id}")
    private String drugId;

    @Value("${elastic.allSearch}")
    private String allSearch;

    public DrugDetailService(DrugRepository drugRepository, DrugEffectRepository drugEffectRepository, DrugStorageConditionRepository conditionRepository, ElasticsearchClient elasticsearchClient) {
        this.drugRepository = drugRepository;
        this.drugEffectRepository = drugEffectRepository;
        this.conditionRepository = conditionRepository;
        this.elasticsearchClient = elasticsearchClient;
    }

    public DrugDetail getDetail(SearchIndexDTO searchIndexDTO) {
        long id = searchIndexDTO.id();
        Optional<Drug> drug = drugRepository.findById(id);
        List<DrugEffect> drugEffectList = drugEffectRepository.findByDrugId(id);
        List<DrugStorageCondition> drugStorageConditions = conditionRepository.findByDrugId(id);
        return drug.map(value -> new DrugDetail(
                id,
                searchIndexDTO.drugName(),
                searchIndexDTO.manufacturer(),
                searchIndexDTO.ingredients(),
                value.getForm(),
                value.getPackaging(),
                value.getAtcCode(),
                value.getTag(),
                value.getApprovalDate(),
                drugStorageConditions,
                drugEffectList
        )).orElse(null);
    }


    public SearchIndexDTO getDetailFromElasticsearch(long id) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s ->
                s.index(allSearch)
                        .query(q -> q
                                .term(t -> t
                                        .field(drugId)
                                        .value(id)
                                )
                        )
        );

        SearchResponse<SearchIndexDTO> response = elasticsearchClient.search(searchRequest,SearchIndexDTO.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null); // Optional 안 쓰고 null
    }




}
