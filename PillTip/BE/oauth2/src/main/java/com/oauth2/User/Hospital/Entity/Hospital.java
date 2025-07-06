package com.oauth2.User.Hospital.Entity;

// ?JPA - 자바 객체와 데이터베이스 테이블 연결
import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;

@Entity
@Table(name = "Hospitals")
@Data
@Builder
public class Hospital {
    @Id
    // ?데이터베이스에서 자동으로 1씩 증가하는 ID를 생성
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hospitalCode;

    @Column(nullable = false)
    private String hospitalName;

    @Column(nullable = false)
    private String hospitalAddress;
} 