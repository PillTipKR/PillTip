package com.oauth2.User.TakingPill.Repositoty;

import com.oauth2.User.TakingPill.Entity.DosageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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


}
