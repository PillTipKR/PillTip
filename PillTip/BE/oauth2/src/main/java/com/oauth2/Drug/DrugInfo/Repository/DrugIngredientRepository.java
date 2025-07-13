package com.oauth2.Drug.DrugInfo.Repository;

import com.oauth2.Drug.DrugInfo.Domain.DrugIngredient;
import com.oauth2.Drug.DrugInfo.Domain.DrugIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DrugIngredientRepository extends JpaRepository<DrugIngredient, DrugIngredientId> {
    List<DrugIngredient> findById_IngredientId(Long ingredientId);

    List<DrugIngredient> findById_DrugId(Long id_drugId);
    Optional<DrugIngredient> findById(DrugIngredientId id);

    @Query("""
        SELECT DISTINCT di.id.drugId
        FROM DrugIngredient di, Ingredient i
        WHERE di.id.ingredientId = i.id
        AND ( i.nameEn like :name OR :name like i.nameEn)
   \s""")
    List<Long> findDrugIdsByIngredientName(@Param("name") String name);
} 