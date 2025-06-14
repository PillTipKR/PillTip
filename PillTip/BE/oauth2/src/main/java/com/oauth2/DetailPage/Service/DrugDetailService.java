package com.oauth2.DetailPage.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.oauth2.DUR.Dto.DurTagDto;
import com.oauth2.DUR.Service.DurTaggingService;
import com.oauth2.DetailPage.Dto.DrugDetail;
import com.oauth2.DetailPage.Dto.EffectDetail;
import com.oauth2.DetailPage.Dto.StorageDetail;
import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Repository.DrugRepository;
import com.oauth2.Search.Dto.SearchIndexDTO;
import com.oauth2.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DrugDetailService {

    private final DrugRepository drugRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final DurTaggingService durTaggingService;

    @Value("${elastic.drug.id}")
    private String drugId;

    @Value("${elastic.allSearch}")
    private String allSearch;

    public DrugDetail getDetail(User user, Long id) throws IOException {
        DurTagDto durTagDto = durTaggingService.generateTagsForDrugs(user, getDetailFromElasticsearch(id)).get(0);

        // 한 번의 쿼리로 Drug과 관련된 DrugEffect, DrugStorageCondition을 가져옵니다.
        Optional<Drug> drug = drugRepository.findDrugWithAllRelations(id);
        return drug.map(value -> DrugDetail.builder()
                .id(id)
                .name(durTagDto.drugName())
                .manufacturer(durTagDto.manufacturer())
                .ingredients(durTagDto.ingredients())
                .packaging(value.getPackaging())
                .form(value.getForm())
                .tag(value.getTag())
                .atcCode(value.getAtcCode())
                .approvalDate(value.getApprovalDate())
                .effectDetails(
                        drug.get().getDrugEffects().stream()
                                .map(e -> new EffectDetail(e.getType(), e.getContent()))
                                .toList()
                )
                .storageDetails(
                        drug.get().getStorageConditions().stream()
                                .map(s -> new StorageDetail(s.getCategory(), s.getValue(), s.isActive()))
                                .toList()
                )
                .durTags(durTagDto.durTags())
                .build()).orElse(null);
    }


    public List<SearchIndexDTO> getDetailFromElasticsearch(long id) throws IOException {
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
                .toList();
    }




}
