package com.oauth2.Drug.Service;

import com.oauth2.Drug.Domain.DrugIngredient;
import com.oauth2.Drug.Repository.DrugIngredientRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DrugIngredientService {
    private final DrugIngredientRepository drugIngredientRepository;

    public DrugIngredientService(DrugIngredientRepository drugIngredientRepository) {
        this.drugIngredientRepository = drugIngredientRepository;
    }

    public List<DrugIngredient> findAll() {
        return drugIngredientRepository.findAll();
    }
    public DrugIngredient save(DrugIngredient drugIngredient) {
        return drugIngredientRepository.save(drugIngredient);
    }
    public void delete(DrugIngredient.DrugIngredientId id) {
        drugIngredientRepository.deleteById(id);
    }
    public DrugIngredient findById(DrugIngredient.DrugIngredientId id) {
        return drugIngredientRepository.findById(id).orElse(null);
    }
} 