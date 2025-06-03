package com.oauth2.Drug.Service;

import com.oauth2.Drug.Domain.DrugCaution;
import com.oauth2.Drug.Repository.DrugCautionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DrugCautionService {
    private final DrugCautionRepository drugCautionRepository;

    public DrugCautionService(DrugCautionRepository drugCautionRepository) {
        this.drugCautionRepository = drugCautionRepository;
    }

    public List<DrugCaution> findAll() {
        return drugCautionRepository.findAll();
    }
    public DrugCaution save(DrugCaution drugCaution) {
        return drugCautionRepository.save(drugCaution);
    }
    public void delete(Long id) {
        drugCautionRepository.deleteById(id);
    }
    public DrugCaution findById(Long id) {
        return drugCautionRepository.findById(id).orElse(null);
    }
} 