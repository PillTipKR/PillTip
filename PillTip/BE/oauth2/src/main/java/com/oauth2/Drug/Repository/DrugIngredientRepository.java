package com.oauth2.Drug.Repository;

import com.oauth2.Drug.Domain.DrugIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DrugIngredientRepository extends JpaRepository<DrugIngredient, DrugIngredient.DrugIngredientId> {
    List<DrugIngredient> findById_IngredientId(Long ingredientId);

    List<DrugIngredient> findById_DrugId(Long id_drugId);
    Optional<DrugIngredient> findById(DrugIngredient.DrugIngredientId id);
} 