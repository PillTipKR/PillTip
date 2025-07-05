// author : mireutale
// description : 문진표 엔티티
package com.oauth2.User.PatientQuestionnaire.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.Util.Encryption.EncryptionConverter;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "patientQuestionnaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientQuestionnaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "questionnaireId")
    private Integer questionnaireId;

    @Column(name = "questionnaireName")
    private String questionnaireName; // 문진표 이름

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "uuid")
    private User user;

    @Column(name = "issueDate")
    private LocalDate issueDate; // 작성일

    @Column(name = "lastModifiedDate")
    private LocalDate lastModifiedDate; // 마지막 수정일

    @Column(columnDefinition = "TEXT")
    private String notes; // 추가 메모

    @Column(name = "medicationInfo", columnDefinition = "TEXT")
    @Convert(converter = EncryptionConverter.class)
    private String medicationInfo;

    @Column(name = "allergyInfo", columnDefinition = "TEXT")
    @Convert(converter = EncryptionConverter.class)
    private String allergyInfo;

    @Column(name = "chronicDiseaseInfo", columnDefinition = "TEXT")
    @Convert(converter = EncryptionConverter.class)
    private String chronicDiseaseInfo;

    @Column(name = "surgeryHistoryInfo", columnDefinition = "TEXT")
    @Convert(converter = EncryptionConverter.class)
    private String surgeryHistoryInfo;
}