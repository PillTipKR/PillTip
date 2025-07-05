package com.oauth2.User.TakingPill.Entity;

import com.oauth2.User.Auth.Entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "dosage_log")
public class DosageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 사용자 기준으로 복약 기록을 저장


    @Column(nullable = false)
    private String medicationName;

    @Column(nullable = false)
    private String alarmName;

    @Column(nullable = false)
    private LocalDateTime scheduledTime; // 복약 예정 시간

    @Column
    private LocalDateTime takenAt; // 실제 복약 완료 시간

    @Column(nullable = false)
    private boolean isTaken = false; // 복약 완료 여부

    @Column
    private LocalDateTime rescheduledTime; // 알림 재전송 시간

    @Column(nullable = false)
    private Boolean isRescheduled = false;

    @Builder
    public DosageLog(User user, String alarmName, String medicationName, LocalDateTime scheduledTime) {
        this.user = user;
        this.medicationName = medicationName;
        this.scheduledTime = scheduledTime;
        this.isTaken = false;
        this.isRescheduled = false;
    }
}
