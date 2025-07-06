package com.oauth2.User.Hospital.Service;

import com.oauth2.User.Hospital.Entity.Hospital;
import com.oauth2.User.Hospital.Repository.HospitalRepository;
import com.oauth2.User.Hospital.dto.HospitalRegistrationRequest;
import com.oauth2.User.Hospital.dto.HospitalRegistrationResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    @Transactional
    // ? 새로운 병원 DB에 등록
    public Hospital createHospital(HospitalRegistrationRequest request) {
        // 중복 체크
        if (hospitalRepository.existsByNameAndAddress(request.getHospitalName(), request.getHospitalAddress())) {
            throw new IllegalArgumentException("이미 동일한 이름과 주소의 병원이 존재합니다.");
        }

        // 병원 코드 생성
        String hospitalCode = generateHospitalCode(request.getHospitalAddress());

        // 병원 코드 중복 체크
        if (hospitalRepository.findByHospitalCode(hospitalCode).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 병원 코드입니다.");
        }

        // 입력된 병원 정보를 가진 객체 생성
        Hospital hospital = Hospital.builder()
                .hospitalCode(hospitalCode)
                .hospitalName(request.getHospitalName())
                .hospitalAddress(request.getHospitalAddress())
                .build();

        // 병원 객체 DB에 저장
        return hospitalRepository.save(hospital);
    }

    @Transactional
    // ? 기존 병원 정보 수정
    public Hospital updateHospital(Long id, HospitalRegistrationRequest request) {
        // 입력된 id에 해당하는 병원 찾기
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("병원을 찾을 수 없습니다."));
        
        // 병원 정보 수정 및 저장
        hospital.setHospitalName(request.getHospitalName());
        hospital.setHospitalAddress(request.getHospitalAddress());
        return hospitalRepository.save(hospital);
    }

    @Transactional
    // ? DB에서 병원 삭제
    public void deleteHospital(Long id) {
        if (!hospitalRepository.existsById(id)) {
            throw new IllegalArgumentException("병원을 찾을 수 없습니다.");
        }
        hospitalRepository.deleteById(id);
    }

    // ? 병원 이름으로 병원 검색
    public List<HospitalRegistrationResponse> searchHospitalByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        return hospitalRepository.findHospitalByName(name.trim())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private HospitalRegistrationResponse convertToResponse(Hospital hospital) {
        HospitalRegistrationResponse response = new HospitalRegistrationResponse();
        response.setId(hospital.getId());
        response.setHospitalCode(hospital.getHospitalCode());
        response.setHospitalName(hospital.getHospitalName());
        response.setHospitalAddress(hospital.getHospitalAddress());
        return response;
    }

    public String generateHospitalCode(String address) {
        String prefix = getPrefixFromAddress(address);
        long count = hospitalRepository.countByHospitalCodeStartingWith(prefix);
        return prefix + (count + 1);
    }

    public Optional<Hospital> findHospitalByCode(String hospitalCode) {
        return hospitalRepository.findByHospitalCode(hospitalCode);
    }

    private String getPrefixFromAddress(String address) {
        if (address.contains("서울")) return "HosSeoul";
        if (address.contains("부산")) return "HosPusan";
        if (address.contains("대구")) return "HosDaegu";
        if (address.contains("인천")) return "HosIncheon";
        if (address.contains("광주")) return "HosGwangju";
        if (address.contains("대전")) return "HosDaejeon";
        if (address.contains("울산")) return "HosUlsan";
        if (address.contains("세종")) return "HosSejong";
        if (address.contains("경기")) return "HosGyeonggi";
        if (address.contains("강원")) return "HosGangwon";
        if (address.contains("충북")) return "HosChungbuk";
        if (address.contains("충남")) return "HosChungnam";
        if (address.contains("전북")) return "HosJeonbuk";
        if (address.contains("전남")) return "HosJeonnam";
        if (address.contains("경북")) return "HosGyeongbuk";
        if (address.contains("경남")) return "HosGyeongnam";
        if (address.contains("제주")) return "HosJeju";
        return "HosEtc";
    }
}
 