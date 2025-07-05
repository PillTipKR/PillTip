// author : mireutale
// description : 사용자 의료 이력 종합 관리 엔티티
package com.oauth2.User.UserInfo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.oauth2.User.Auth.Entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "userSensitiveInfo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSensitiveInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 약물 정보 (JSON 형태로 저장)
    @Column(name = "medication_info", columnDefinition = "TEXT")
    private String medicationInfo;

    // 알러지 정보 (JSON 형태로 저장)
    @Column(name = "allergy_info", columnDefinition = "TEXT")
    private String allergyInfo;

    // 만성질환 정보 (JSON 형태로 저장)
    @Column(name = "chronic_disease_info", columnDefinition = "TEXT")
    private String chronicDiseaseInfo;

    // 수술이력 정보 (JSON 형태로 저장)
    @Column(name = "surgery_history_info", columnDefinition = "TEXT")
    private String surgeryHistoryInfo;
} 