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

    @Column(name = "questionnaire_name")
    private String questionnaireName; // 문진표 이름

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "uuid")
    private User user;

    @Column(name = "real_name")
    private String realName;

    @Column(name = "address")
    private String address;

    @Column(name = "issue_date")
    private LocalDate issueDate; // 작성일

    @Column(name = "last_modified_date")
    private LocalDate lastModifiedDate; // 마지막 수정일

    @Column(columnDefinition = "TEXT")
    private String notes; // 추가 메모
}
