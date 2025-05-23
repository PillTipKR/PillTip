package com.oauth2.AutoComplete.Service;

import com.oauth2.Drug.Domain.Drug;
import com.oauth2.Drug.Domain.Ingredient;
import com.oauth2.Drug.Repository.DrugRepository;
import com.oauth2.Drug.Repository.IngredientRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSyncService {

    private final RedisService redisService;

    private final DrugRepository drugRepository;

    private final IngredientRepository ingredientRepository;

    @Value("${redis.drug.drug}")
    private String hashDrug;
    @Value("${redis.drug.manufacturer}")
    private String hashMaufacturer;
    @Value("${redis.drug.image}")
    private String hashImage;

    public DataSyncService(RedisService redisService, DrugRepository drugRepository, IngredientRepository ingredientRepository) {
        this.redisService = redisService;
        this.drugRepository = drugRepository;
        this.ingredientRepository = ingredientRepository;
    }

    //@PostConstruct // redis 캐시 설정
    public void syncDataWithRedis() {
        List<Drug> drugs = drugRepository.findAll();
        List<Ingredient> ingredients = ingredientRepository.findAll();
        for (Drug drug : drugs) {
            String drugId = String.valueOf(drug.getId());
            redisService.addAutocompleteSuggestion(drugId, hashDrug,drug.getName());
            redisService.addAutocompleteSuggestion(drugId, hashMaufacturer, drug.getManufacturer());
            //redisService.addAutocompleteSuggestion(drugId, hashImage, null); // 추후에 이미지 삽입 방법 결정 시 추가
        }

        /*
        for(Ingredient ingredient : ingredients){
            String ingredientId = ingredient.getId();
            redisService.addAutocompleteSuggestion(ingredientId, "ingredient", ingredient.getNameKr());
        }
        */
    }
}
