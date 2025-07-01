package com.oauth2.User.repository;

import com.oauth2.User.entity.TakingPill;
import com.oauth2.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TakingPillRepository extends JpaRepository<TakingPill, Long> {
    List<TakingPill> findByUser(User user);
    List<TakingPill> findByUserAndMedicationId(User user, Long medicationId);
    Optional<TakingPill> findById(Long id);

    @Query("SELECT DISTINCT tp FROM TakingPill tp LEFT JOIN FETCH tp.dosageSchedules WHERE tp.user = :user")
    List<TakingPill> findByUserWithDosageSchedules(@Param("user") User user);
}