package com.oauth2.Drug.Repository;

import com.oauth2.Drug.Domain.DrugStorageCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugStorageConditionRepository extends JpaRepository<DrugStorageCondition, Long> {
} 