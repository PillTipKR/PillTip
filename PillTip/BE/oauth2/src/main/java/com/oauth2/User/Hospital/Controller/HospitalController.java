package com.oauth2.User.Hospital.Controller;

import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Hospital.Entity.Hospital;
import com.oauth2.User.Hospital.Service.HospitalService;
import com.oauth2.User.Hospital.dto.HospitalRegistrationRequest;
import com.oauth2.User.Hospital.dto.HospitalRegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalController {
    private final HospitalService hospitalService;

    // ? 병원 등록
    @PostMapping("")
    public ResponseEntity<ApiResponse<Hospital>> createHospital(@RequestBody HospitalRegistrationRequest request) {
        try {
            Hospital saved = hospitalService.createHospital(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("병원 등록 성공", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("병원 등록 실패", null));
        }
    }

    // ? 병원 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Hospital>> updateHospital(@PathVariable Long id, @RequestBody HospitalRegistrationRequest request) {
        try {
            Hospital updated = hospitalService.updateHospital(id, request);
            return ResponseEntity.status(HttpStatus.OK)
                                .body(ApiResponse.success("병원 수정 성공", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("병원 수정 실패", null));
        }
    }

    // ? 병원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteHospital(@PathVariable Long id) {
        try {
            hospitalService.deleteHospital(id);
            return ResponseEntity.status(HttpStatus.OK)
                                .body(ApiResponse.success("병원 삭제 성공", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("병원 삭제 실패", null));
        }
    }

    // ? 병원 이름으로 병원 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<HospitalRegistrationResponse>>> searchHospitalByName(@RequestParam String name) {
        try {
            List<HospitalRegistrationResponse> result = hospitalService.searchHospitalByName(name);
            return ResponseEntity.status(HttpStatus.OK)
                                .body(ApiResponse.success("병원 검색 성공", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error("병원 검색 실패", null));
        }
    }
}