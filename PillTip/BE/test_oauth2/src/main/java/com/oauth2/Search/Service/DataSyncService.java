package com.oauth2.Search.Service;

import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Repository.DrugRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSyncService {

    private final RedisService redisService;

    private final DrugRepository drugRepository;


    @Value("${redis.drug.drug}")
    private String hashDrug;
    @Value("${redis.drug.manufacturer}")
    private String hashMaufacturer;

    public DataSyncService(RedisService redisService, DrugRepository drugRepository) {
        this.redisService = redisService;
        this.drugRepository = drugRepository;
    }

    public void syncDataWithRedis() {
        List<Drug> drugs = drugRepository.findAll();
        for (Drug drug : drugs) {
            String drugId = String.valueOf(drug.getId());
            redisService.addAutocompleteSuggestion(drugId, hashDrug,drug.getName());
            redisService.addAutocompleteSuggestion(drugId, hashMaufacturer, drug.getManufacturer());
        }
    }
}
