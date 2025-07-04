package com.oauth2.Drug.DrugImport.Service;

import com.oauth2.Drug.DrugImport.Domain.DrugIngredient;
import com.oauth2.Drug.DrugImport.Domain.DrugIngredientId;
import com.oauth2.Drug.DrugImport.Repository.DrugIngredientRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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
    public void delete(DrugIngredientId id) {
        drugIngredientRepository.deleteById(id);
    }
    public DrugIngredient findByIngredientId(DrugIngredientId id) {
        return drugIngredientRepository.findById(id).orElse(null);
    }
    public Optional<DrugIngredient> findById(DrugIngredientId id){
        return drugIngredientRepository.findById(id);
    }
    public List<DrugIngredient> findByDrugId(Long id) {
        return drugIngredientRepository.findById_DrugId(id);
    }
} 