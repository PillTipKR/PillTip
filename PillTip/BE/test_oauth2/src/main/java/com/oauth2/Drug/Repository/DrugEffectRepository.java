package com.oauth2.Drug.Repository;


import com.oauth2.Drug.Domain.DrugEffect;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugEffectRepository extends JpaRepository<DrugEffect, Long> {
} 