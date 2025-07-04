package com.oauth2.User.TakingPill.Repositoty;

import com.oauth2.User.TakingPill.Entity.DosageSchedule;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DosageScheduleRepository extends JpaRepository<DosageSchedule, Long> {
    List<DosageSchedule> findByTakingPill(TakingPill takingPill);
} 