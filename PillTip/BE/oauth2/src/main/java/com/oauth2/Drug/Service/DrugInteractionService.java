package com.oauth2.Drug.Service;

import com.oauth2.Drug.Domain.DrugInteraction;
import com.oauth2.Drug.Repository.DrugInteractionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DrugInteractionService {
    private final DrugInteractionRepository drugInteractionRepository;

    public DrugInteractionService(DrugInteractionRepository drugInteractionRepository) {
        this.drugInteractionRepository = drugInteractionRepository;
    }

    public List<DrugInteraction> findAll() {
        return drugInteractionRepository.findAll();
    }
    public DrugInteraction save(DrugInteraction drugInteraction) {
        return drugInteractionRepository.save(drugInteraction);
    }
    public void delete(Long id) {
        drugInteractionRepository.deleteById(id);
    }
    public DrugInteraction findById(Long id) {
        return drugInteractionRepository.findById(id).orElse(null);
    }
} 