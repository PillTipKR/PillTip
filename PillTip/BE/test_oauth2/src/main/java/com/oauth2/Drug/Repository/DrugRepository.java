package com.oauth2.Drug.Repository;

import com.oauth2.Drug.Domain.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DrugRepository extends JpaRepository<Drug, Long> {
    Optional<Drug> findByName(String name);
    List<Drug> findByIdIn(List<Long> ids);
} 