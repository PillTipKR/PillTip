package com.oauth2.User.service;

import com.oauth2.User.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    public boolean existsByHospitalCode(String hospitalCode) {
        return hospitalRepository.findByHospitalCode(hospitalCode).isPresent();
    }

    public String generateHospitalCode(String address) {
        String prefix;
        if (address.contains("서울")) {
            prefix = "HosSeoul";
        } else if (address.contains("부산")) {
            prefix = "HosPusan";
        } else if (address.contains("대구")) {
            prefix = "HosDaegu";
        } else if (address.contains("인천")) {
            prefix = "HosIncheon";
        } else if (address.contains("광주")) {
            prefix = "HosGwangju";
        } else if (address.contains("대전")) {
            prefix = "HosDaejeon";
        } else if (address.contains("울산")) {
            prefix = "HosUlsan";
        } else if (address.contains("세종")) {
            prefix = "HosSejong";
        } else if (address.contains("경기")) {
            prefix = "HosGyeonggi";
        } else if (address.contains("강원")) {
            prefix = "HosGangwon";
        } else if (address.contains("충북")) {
            prefix = "HosChungbuk";
        } else if (address.contains("충남")) {
            prefix = "HosChungnam";
        } else if (address.contains("전북")) {
            prefix = "HosJeonbuk";
        } else if (address.contains("전남")) {
            prefix = "HosJeonnam";
        } else if (address.contains("경북")) {
            prefix = "HosGyeongbuk";
        } else if (address.contains("경남")) {
            prefix = "HosGyeongnam";
        } else if (address.contains("제주")) {
            prefix = "HosJeju";
        } else {
            prefix = "HosEtc";
        }
        // 일련번호 구하기
        long count = hospitalRepository.findAll().stream()
            .filter(h -> h.getHospitalCode() != null && h.getHospitalCode().startsWith(prefix))
            .count();
        return prefix + (count + 1);
    }
} 