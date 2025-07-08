package com.oauth2.User.TakingPill.Repositoty;

import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.TakingPill.Entity.DosageLog;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DosageLogRepository extends JpaRepository<DosageLog, Long> {

    @Query("SELECT dl FROM DosageLog dl " +
            "JOIN dl.user u " +
            "WHERE u.id = :userId " +
            "AND DATE(dl.scheduledTime) = :targetDate")
    List<DosageLog> findByUserAndDate(
            @Param("userId") Long userId,
            @Param("targetDate") LocalDate targetDate
    );

    @Query("SELECT dl FROM DosageLog dl " +
            "JOIN dl.user u " +
            "WHERE u.id = :userId " +
            "AND DATE(dl.rescheduledTime) = :targetDate")
    List<DosageLog> findByUserAndRescheduledDate(
            @Param("userId") Long userId,
            @Param("targetDate") LocalDate targetDate
    );

    List<DosageLog> findByTakingPillAndScheduledTimeAfter(TakingPill pill, LocalDateTime time);

    List<DosageLog> findByUserAndTakingPill(User user, TakingPill takingPill);

    void deleteAllByUserAndMedicationNameAndScheduledTimeAfter(User user, String medicationName, LocalDateTime time);

    void deleteAllByUserAndMedicationNameAndScheduledTimeBetween(User user, String medicationName, LocalDateTime from, LocalDateTime to);


}
