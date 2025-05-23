package com.oauth2.Drug.Repository;

import com.oauth2.Drug.Domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByNameKr(String nameKr);
} 