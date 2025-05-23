package com.oauth2.AutoComplete.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    @Value("${redis.index}")
    private String key;

    @Value("${redis.drug.drug}")
    private String hashDrug;
    @Value("${redis.drug.manufacturer}")
    private String hashMaufacturer;
    @Value("${redis.drug.image}")
    private String hashImage;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addAutocompleteSuggestion(String drugId, String column, String input) {
        redisTemplate.opsForHash().put(key + drugId, column, input);
    }

    public String getDrugNameFromCache(String drugId) {
        return (String) redisTemplate.opsForHash().get(key + drugId, hashDrug);
    }

    public String getManufacturerFromCache(String drugId) {
        return (String) redisTemplate.opsForHash().get(key + drugId, hashMaufacturer);
    }

    public String getImageUrlFromCache(String drugId) {
        return (String) redisTemplate.opsForHash().get(key + drugId, hashImage);
    }
}
