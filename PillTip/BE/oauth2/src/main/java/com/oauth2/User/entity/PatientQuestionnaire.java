// author : mireutale
// description : 문진표 엔티티
package com.oauth2.User.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @Column(name = "questionnaire_id")
    private Integer questionnaireId;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "uuid")
    private User user;

    @Column(name = "questionnaire_name")
    private String questionnaireName; // 문진표 이름

    @Column(name = "issue_date")
    private LocalDate issueDate; // 작성일

    @Column(columnDefinition = "TEXT")
    private String notes; // 추가 메모
}
