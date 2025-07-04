package com.oauth2.Drug.DrugImport.Repository;

import com.oauth2.Drug.DrugImport.Domain.DrugIngredient;
import com.oauth2.Drug.DrugImport.Domain.DrugIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DrugIngredientRepository extends JpaRepository<DrugIngredient, DrugIngredientId> {
    List<DrugIngredient> findById_IngredientId(Long ingredientId);

    List<DrugIngredient> findById_DrugId(Long id_drugId);
    Optional<DrugIngredient> findById(DrugIngredientId id);
} 