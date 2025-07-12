package com.oauth2.User.TakingPill.Repositoty;

import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.Auth.Entity.User;
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

   @Query("SELECT t FROM TakingPill t WHERE t.user.id = :userId")
   List<TakingPill> findAllByUserId(@Param("userId") Long userId);

}