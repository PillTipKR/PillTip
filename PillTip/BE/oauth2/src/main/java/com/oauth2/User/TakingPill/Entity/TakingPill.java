package com.oauth2.User.TakingPill.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.Util.Encryption.EncryptionConverter;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Convert(converter = EncryptionConverter.class)
    private String medicationName;

    @Column(name = "start_year", nullable = false)
    private Integer startYear;

    @Column(name = "start_month", nullable = false)
    private Integer startMonth;

    @Column(name = "start_day", nullable = false)
    private Integer startDay;

    @Column(name = "end_year", nullable = false)
    private Integer endYear;

    @Column(name = "end_month", nullable = false)
    private Integer endMonth;

    @Column(name = "end_day", nullable = false)
    private Integer endDay;

    @Column(name = "alarm_name", nullable = false)
    @Convert(converter = EncryptionConverter.class)
    private String alarmName;

    @Column(name = "days_of_week", nullable = false)
    @Convert(converter = EncryptionConverter.class)
    private String daysOfWeek; // JSON 형태로 저장 (["MON", "TUE", "WED"])

    @Column(name = "dosage_amount", nullable = false)
    private Double dosageAmount; // 복용량 (0.25부터 가능)

    @JsonManagedReference
    @OneToMany(mappedBy = "takingPill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DosageSchedule> dosageSchedules = new ArrayList<>();

    @Builder
    public TakingPill(User user, Long medicationId, String medicationName, 
                     Integer startYear, Integer startMonth, Integer startDay,
                     Integer endYear, Integer endMonth, Integer endDay,
                     String alarmName, String daysOfWeek, Double dosageAmount) {
        this.user = user;
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.endYear = endYear;
        this.endMonth = endMonth;
        this.endDay = endDay;
        this.alarmName = alarmName;
        this.daysOfWeek = daysOfWeek;
        this.dosageAmount = dosageAmount;
    }
} 
