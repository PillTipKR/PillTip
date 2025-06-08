package com.oauth2.Drug.Repository;

import com.oauth2.DUR.Domain.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Long> {
} 