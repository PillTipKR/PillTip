package com.oauth2.Drug.DrugImport.Service;

import com.oauth2.Drug.DrugImport.Domain.DrugEffect;
import com.oauth2.Drug.DrugImport.Repository.DrugEffectRepository;
import org.springframework.stereotype.Service;

@Service
public class DrugEffectService {
    private final DrugEffectRepository drugEffectRepository;

    public DrugEffectService(DrugEffectRepository drugEffectRepository) {
        this.drugEffectRepository = drugEffectRepository;
    }

    public DrugEffect findById(Long id) {
        return drugEffectRepository.findById(id).orElse(null);
    }

    public DrugEffect save(DrugEffect drugEffect) {
        return drugEffectRepository.save(drugEffect);
    }
}