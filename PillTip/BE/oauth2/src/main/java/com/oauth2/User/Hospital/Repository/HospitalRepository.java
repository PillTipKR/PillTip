package com.oauth2.User.Hospital.Repository;

// ? 기본적인 CRUD 메서드 사용을 위한 라이브러리, findBy, save, delete, findAll ...
import org.springframework.data.jpa.repository.JpaRepository; 
import com.oauth2.User.Hospital.Entity.Hospital;
// ? null 처리를 위한 라이브러리
import java.util.Optional; 
// ? 리스트 처리를 위한 라이브러리
import java.util.List; 

// ? JpaRepository를 상속, Hospital과 Long을 파라미터로 받음
public interface HospitalRepository extends JpaRepository<Hospital, Long> { 
    Optional<Hospital> findByHospitalCode(String hospitalCode); // ? 병원 코드로 병원 찾기
    boolean existsByNameAndAddress(String name, String address); // ? 병원 이름과 주소로 병원 존재 여부 확인
    List<Hospital> findHospitalByName(String name); // ? 병원 이름으로 병원 찾기
    long countByHospitalCodeStartingWith(String prefix); // ? 병원 코드 접두사로 병원 개수 카운트
} 