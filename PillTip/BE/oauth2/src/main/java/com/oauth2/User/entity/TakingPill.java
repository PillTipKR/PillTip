package com.oauth2.User.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "taking_pill")
public class TakingPill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "medication_id", nullable = false)
    private Long medicationId;

    @Column(name = "medication_name", nullable = false)
    private String medicationName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "alarm_name", nullable = false)
    private String alarmName;

    @Column(name = "days_of_week", nullable = false)
    private String daysOfWeek; // JSON 형태로 저장 (["MON", "TUE", "WED"])

    @Column(name = "dosage_amount", nullable = false)
    private Double dosageAmount; // 복용량 (0.25부터 가능)

    @JsonManagedReference
    @OneToMany(mappedBy = "takingPill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DosageSchedule> dosageSchedules = new ArrayList<>();

    @Builder
    public TakingPill(User user, Long medicationId, String medicationName, LocalDate startDate, 
                     LocalDate endDate, String alarmName, String daysOfWeek, Double dosageAmount) {
        this.user = user;
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.alarmName = alarmName;
        this.daysOfWeek = daysOfWeek;
        this.dosageAmount = dosageAmount;
    }
} 