package com.oauth2.User.Hospital.Controller;

import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Hospital.Entity.Hospital;
import com.oauth2.User.Hospital.Service.HospitalService;
import com.oauth2.User.Hospital.dto.HospitalRequest;
import com.oauth2.User.Hospital.dto.HospitalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalController {
    private final HospitalService hospitalService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Hospital>> createHospital(@RequestBody HospitalRequest request) {
        try {
            Hospital saved = hospitalService.createHospital(request);
            return ResponseEntity.ok(ApiResponse.success("병원 등록 성공", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Hospital>> updateHospital(@PathVariable Long id, @RequestBody HospitalRequest request) {
        try {
            Hospital updated = hospitalService.updateHospital(id, request);
            return ResponseEntity.ok(ApiResponse.success("병원 수정 성공", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteHospital(@PathVariable Long id) {
        try {
            hospitalService.deleteHospital(id);
            return ResponseEntity.ok(ApiResponse.success("병원 삭제 성공", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<HospitalResponse>>> searchHospitalByName(@RequestParam String name) {
        List<HospitalResponse> result = hospitalService.searchHospitalByName(name);
        return ResponseEntity.ok(ApiResponse.success("병원 검색 성공", result));
    }
}