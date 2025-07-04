package com.oauth2.Drug.DrugImport.Repository;

import com.oauth2.Drug.DrugImport.Domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByNameKr(String nameKr);

    List<Ingredient> findByNameEn(String nameEng);

    Optional<Ingredient> findById(Long id);
} 