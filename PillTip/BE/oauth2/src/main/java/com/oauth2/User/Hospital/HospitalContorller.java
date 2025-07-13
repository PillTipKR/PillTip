package com.oauth2.User.Hospital;

import com.oauth2.User.Auth.Dto.ApiResponse;
import com.oauth2.User.Hospital.HospitalMessageConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalContorller {
    private final HospitalService hospitalService;
    private final HospitalRepository hospitalRepository;

    // 병원 등록
    @PostMapping("")
    public ResponseEntity<ApiResponse<Hospital>> createHospital(@RequestBody HospitalRequest request) {
        // 동일한 이름과 주소의 병원이 이미 존재하는지 확인
        boolean existsSame = hospitalRepository.findAll().stream()
            .anyMatch(h -> h.getName().equals(request.getName()) && h.getAddress().equals(request.getAddress()));
        if (existsSame) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HospitalMessageConstants.HOSPITAL_ALREADY_EXISTS, null));
        }
        // hospitalCode는 주소 기반으로 자동 생성
        String hospitalCode = hospitalService.generateHospitalCode(request.getAddress());
        if (hospitalRepository.findByHospitalCode(hospitalCode).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HospitalMessageConstants.HOSPITAL_CODE_ALREADY_EXISTS, null));
        }
        Hospital hospital = Hospital.builder()
                .hospitalCode(hospitalCode)
                .name(request.getName())
                .address(request.getAddress())
                .build();
        Hospital saved = hospitalRepository.save(hospital);
        return ResponseEntity.ok(ApiResponse.success(HospitalMessageConstants.HOSPITAL_CREATE_SUCCESS, saved));
    }

    // 병원 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Hospital>> updateHospital(@PathVariable Long id, @RequestBody HospitalRequest request) {
        try {
            Hospital hospital = hospitalRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(HospitalMessageConstants.HOSPITAL_NOT_FOUND));
            hospital.setName(request.getName());
            hospital.setAddress(request.getAddress());
            Hospital updated = hospitalRepository.save(hospital);
            return ResponseEntity.ok(ApiResponse.success(HospitalMessageConstants.HOSPITAL_UPDATE_SUCCESS, updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HospitalMessageConstants.HOSPITAL_NOT_FOUND, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HospitalMessageConstants.HOSPITAL_UPDATE_FAILED, null));
        }
    }

    // 병원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteHospital(@PathVariable Long id) {
        if (!hospitalRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(HospitalMessageConstants.HOSPITAL_NOT_FOUND, null));
        }
        hospitalRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(HospitalMessageConstants.HOSPITAL_DELETE_SUCCESS, null));
    }

    // 병원 이름으로 병원 id, hospitalCode 조회
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<java.util.List<HospitalSimpleResponse>>> searchHospitalByName(@RequestParam String name) {
        java.util.List<HospitalSimpleResponse> result = hospitalRepository.findAll().stream()
            .filter(h -> h.getName().contains(name))
            .map(h -> new HospitalSimpleResponse(h.getId(), h.getHospitalCode(), h.getName(), h.getAddress()))
            .toList();
        return ResponseEntity.ok(ApiResponse.success(HospitalMessageConstants.HOSPITAL_SEARCH_SUCCESS, result));
    }

    // 병원 등록/수정 요청 DTO
    public static class HospitalRequest {
        private String name;
        private String address;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class HospitalSimpleResponse {
        private Long id;
        private String hospitalCode;
        private String name;
        private String address;
        public HospitalSimpleResponse(Long id, String hospitalCode, String name, String address) {
            this.id = id;
            this.hospitalCode = hospitalCode;
            this.name = name;
            this.address = address;
        }
        public Long getId() { return id; }
        public String getHospitalCode() { return hospitalCode; }
        public String getName() { return name; }
        public String getAddress() { return address; }
    }
}
