package com.oauth2.User.PatientQuestionnaire.Service;

import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.PatientQuestionnaire.Entity.QuestionnaireQRUrl;
import com.oauth2.User.PatientQuestionnaire.Repository.QuestionnaireQRUrlRepository;
import com.oauth2.User.PatientQuestionnaire.Dto.QuestionnaireQRUrlResponse;
import com.oauth2.User.Hospital.HospitalService;
import com.oauth2.User.Auth.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionnaireQRUrlService {
    
    private final QuestionnaireQRUrlRepository qrUrlRepository;
    private final TokenService tokenService;
    private final HospitalService hospitalService;
    
    /**
     * 사용자의 QR URL 생성 또는 업데이트
     */
    @Transactional
    public QuestionnaireQRUrlResponse generateOrUpdateQRUrl(User user, String hospitalCode) {
        // 1. 병원 코드 유효성 검사
        if (!hospitalService.existsByHospitalCode(hospitalCode)) {
            throw new IllegalArgumentException("유효하지 않은 병원 코드입니다.");
        }
        
        // 2. 기존 QR URL 삭제 (새로운 URL 생성 전)
        qrUrlRepository.deleteByUser(user);
        
        // 3. JWT 토큰 생성 (3분)
        String jwtToken = tokenService.createCustomJwtToken(
            user.getId(),
            hospitalCode,
            180
        );
        
        // 4. QR URL 생성 (앱에서 접근할 수 있는 형태)
        String qrUrl = String.format("localhost:3000/questionnaire/public/%s?token=%s",
            user.getId(),
            jwtToken);
        
        String realName = user.getRealName();
        // 5. DB에 저장
        QuestionnaireQRUrl qrUrlEntity = QuestionnaireQRUrl.builder()
            .user(user)
            .qrUrl(qrUrl)
            .hospitalCode(hospitalCode)
            .build();
        
        QuestionnaireQRUrl savedQrUrl = qrUrlRepository.save(qrUrlEntity);
        
        // 6. 응답 생성
        return QuestionnaireQRUrlResponse.builder()
            .qrUrl(savedQrUrl.getQrUrl())
            .realName(realName)
            .build();
    }
    
    /**
     * 사용자의 QR URL 조회
     */
    public QuestionnaireQRUrlResponse getQRUrl(User user, String hospitalCode) {
        QuestionnaireQRUrl qrUrl = qrUrlRepository.findByUser(user)
            .orElseThrow(() -> new IllegalArgumentException("QR URL이 존재하지 않습니다."));
        
        String realName = user.getRealName();
        return QuestionnaireQRUrlResponse.builder()
            .qrUrl(qrUrl.getQrUrl())
            .realName(realName)
            .build();
    }

    public List<QuestionnaireQRUrlResponse> getAllQRUrl(String hospitalCode) {
        List<QuestionnaireQRUrl> qrUrl = qrUrlRepository.findByHospitalCodeWithUser(hospitalCode);
        if (qrUrl.isEmpty()) {
            throw new IllegalArgumentException("QR URL이 존재하지 않습니다.");
        }
        return qrUrl.stream()
            .map(qr -> QuestionnaireQRUrlResponse.builder()
                .qrUrl(qr.getQrUrl())
                .realName(qr.getUser().getRealName())
                .build())
            .collect(Collectors.toList());
    }
    
    /**
     * 사용자의 QR URL 삭제
     */
    @Transactional
    public void deleteQRUrl(User user) {
        qrUrlRepository.deleteByUser(user);
    }
} 