package com.oauth2.Drug.Service;

import com.oauth2.Drug.Domain.DrugStorageCondition;
import com.oauth2.Drug.Repository.DrugStorageConditionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DrugStorageConditionService {
    private final DrugStorageConditionRepository drugStorageConditionRepository;

    public DrugStorageConditionService(DrugStorageConditionRepository drugStorageConditionRepository) {
        this.drugStorageConditionRepository = drugStorageConditionRepository;
    }

    public List<DrugStorageCondition> findAll() {
        return drugStorageConditionRepository.findAll();
    }
    public DrugStorageCondition save(DrugStorageCondition drugStorageCondition) {
        return drugStorageConditionRepository.save(drugStorageCondition);
    }
    public void delete(Long id) {
        drugStorageConditionRepository.deleteById(id);
    }
    public DrugStorageCondition findById(Long id) {
        return drugStorageConditionRepository.findById(id).orElse(null);
    }
} 