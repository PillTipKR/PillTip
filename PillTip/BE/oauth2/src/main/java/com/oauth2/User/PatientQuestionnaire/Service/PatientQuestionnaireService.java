package com.oauth2.User.PatientQuestionnaire.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireRequest;
import com.oauth2.User.PatientQuestionnaire.Dto.PatientQuestionnaireSummaryResponse;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.PatientQuestionnaire.Repository.PatientQuestionnaireRepository;
import com.oauth2.User.UserInfo.Service.UserService;
import com.oauth2.User.TakingPill.Service.TakingPillService;
import com.oauth2.User.TakingPill.Dto.TakingPillRequest;
import com.oauth2.Util.Encryption.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientQuestionnaireService {
    private static final Logger logger = LoggerFactory.getLogger(PatientQuestionnaireService.class);
    
    private final PatientQuestionnaireRepository questionnaireRepository;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final TakingPillService takingPillService;
    private final EncryptionUtil encryptionUtil;

    @Transactional
    public PatientQuestionnaire createQuestionnaire(User user, PatientQuestionnaireRequest request) throws JsonProcessingException {
        // Update user realName and address
        userService.updatePersonalInfo(user, request.getRealName(), request.getAddress());
        userService.updatePhoneNumber(user, request.getPhoneNumber());
        String medicationInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getMedicationInfo(), "medicationId")
        );
        String allergyInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getAllergyInfo(), "allergyName")
        );
        String chronicDiseaseInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getChronicDiseaseInfo(), "chronicDiseaseName")
        );
        String surgeryHistoryInfoJson = objectMapper.writeValueAsString(
                toKeyedList(request.getSurgeryHistoryInfo(), "surgeryHistoryName")
        );

        PatientQuestionnaire questionnaire = PatientQuestionnaire.builder()
                .user(user)
                .questionnaireName(request.getQuestionnaireName())
                .notes(request.getNotes())
                .issueDate(LocalDate.now())
                .lastModifiedDate(LocalDate.now())
                .medicationInfo(medicationInfoJson)
                .allergyInfo(allergyInfoJson)
                .chronicDiseaseInfo(chronicDiseaseInfoJson)
                .surgeryHistoryInfo(surgeryHistoryInfoJson)
                .build();
        
        PatientQuestionnaire savedQuestionnaire = questionnaireRepository.save(questionnaire);
        
        // 문진표 생성 완료 후 별도로 복약 등록 처리
        processMedicationSync(user, request.getMedicationInfo());
        
        return savedQuestionnaire;
    }

    private List<Map<String, ?>> toKeyedList(List<PatientQuestionnaireRequest.InfoItem> list, String keyName) {
        if (list == null) return null;
        return list.stream()
                .map(item -> {
                    String value = null;
                    if ("medicationId".equals(keyName)) {
                        value = item.getMedicationId();
                        // medicationId인 경우 medicationName도 함께 저장
                        return Map.of(
                                keyName, value,
                                "medicationName", item.getMedicationName() != null ? item.getMedicationName() : "",
                                "submitted", item.isSubmitted()
                        );
                    } else if ("allergyName".equals(keyName)) {
                        value = item.getAllergyName();
                    } else if ("chronicDiseaseName".equals(keyName)) {
                        value = item.getChronicDiseaseName();
                    } else if ("surgeryHistoryName".equals(keyName)) {
                        value = item.getSurgeryHistoryName();
                    } else {
                        value = item.getId();
                    }
                    return Map.of(
                            keyName, value,
                            "submitted", item.isSubmitted()
                    );
                })
                .collect(Collectors.toList());
    }

    public List<PatientQuestionnaireSummaryResponse> getUserQuestionnaireSummaries(User user) {
        return questionnaireRepository.findByUser(user).stream()
            .map(q -> new PatientQuestionnaireSummaryResponse(
                q.getQuestionnaireId(),
                q.getQuestionnaireName(),
                q.getIssueDate(),
                q.getLastModifiedDate()
            ))
            .collect(Collectors.toList());
    }

    public PatientQuestionnaire getQuestionnaireById(User user, Integer id) {
        PatientQuestionnaire questionnaire = questionnaireRepository.findByIdWithUser(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        
        // 본인 문진표만 조회 가능
        if (!questionnaire.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 조회할 수 있습니다.");
        }
        
        return questionnaire;
    }

    @Transactional
    public void deleteQuestionnaire(User user, Integer id) {
        PatientQuestionnaire q = questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        if (!q.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 삭제할 수 있습니다.");
        }
        questionnaireRepository.delete(q);
    }

    @Transactional
    public PatientQuestionnaire updateQuestionnaire(User user, Integer id, PatientQuestionnaireRequest request) throws JsonProcessingException {
        // Update user realName and address
        userService.updatePersonalInfo(user, request.getRealName(), request.getAddress());
        userService.updatePhoneNumber(user, request.getPhoneNumber());
        PatientQuestionnaire q = questionnaireRepository.findByIdWithUser(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        if (!q.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 수정할 수 있습니다.");
        }
        q.setQuestionnaireName(request.getQuestionnaireName());
        q.setNotes(request.getNotes());
        q.setMedicationInfo(objectMapper.writeValueAsString(toKeyedList(request.getMedicationInfo(), "medicationId")));
        q.setAllergyInfo(objectMapper.writeValueAsString(toKeyedList(request.getAllergyInfo(), "allergyName")));
        q.setChronicDiseaseInfo(objectMapper.writeValueAsString(toKeyedList(request.getChronicDiseaseInfo(), "chronicDiseaseName")));
        q.setSurgeryHistoryInfo(objectMapper.writeValueAsString(toKeyedList(request.getSurgeryHistoryInfo(), "surgeryHistoryName")));
        q.setLastModifiedDate(LocalDate.now());
        
        PatientQuestionnaire updatedQuestionnaire = q;
        
        // 문진표 수정 완료 후 별도로 복약 등록 처리
        processMedicationSync(user, request.getMedicationInfo());
        
        return updatedQuestionnaire;
    }

    @Transactional
    public List<PatientQuestionnaireSummaryResponse> deleteQuestionnaireAndReturnList(User user, Integer id) {
        PatientQuestionnaire q = questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
        if (!q.getUser().getId().equals(user.getId())) {
            throw new SecurityException("본인 문진표만 삭제할 수 있습니다.");
        }
        questionnaireRepository.delete(q);
        // 삭제 후 남아있는 리스트 반환
        return getUserQuestionnaireSummaries(user);
    }

    // 사용자의 최신 문진표 조회
    public PatientQuestionnaire getLatestQuestionnaireByUser(User user) {
        return questionnaireRepository.findTopByUserOrderByIssueDateDesc(user)
                .orElse(null);
    }

    // 소유자 검증 없이 문진표 조회
    public PatientQuestionnaire getQuestionnaireByIdPublic(Integer id) {
        return questionnaireRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("문진표를 찾을 수 없습니다."));
    }

    /**
     * 문진표에 등록된 복약기록을 takingPill에 자동 등록하는 메서드 (별도 처리)
     */
    private void processMedicationSync(User user, List<PatientQuestionnaireRequest.InfoItem> medicationInfo) {
        if (medicationInfo == null || medicationInfo.isEmpty()) {
            return;
        }

        // 별도 스레드에서 비동기적으로 처리
        new Thread(() -> {
            try {
                syncMedicationToTakingPill(user, medicationInfo);
            } catch (Exception e) {
                logger.error("Failed to sync medication to takingPill for user {}: {}", user.getId(), e.getMessage(), e);
            }
        }).start();
    }

    /**
     * 문진표에 등록된 복약기록을 takingPill에 자동 등록하는 메서드
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    private void syncMedicationToTakingPill(User user, List<PatientQuestionnaireRequest.InfoItem> medicationInfo) {
        if (medicationInfo == null || medicationInfo.isEmpty()) {
            return;
        }

        // submit 값과 관계없이 모든 약물 처리
        List<PatientQuestionnaireRequest.InfoItem> validMedications = medicationInfo.stream()
                .filter(item -> item.getMedicationId() != null && !item.getMedicationId().trim().isEmpty())
                .collect(Collectors.toList());

        for (PatientQuestionnaireRequest.InfoItem medication : validMedications) {
            try {
                Long medicationId = Long.parseLong(medication.getMedicationId());
                String medicationName = medication.getMedicationName() != null ? medication.getMedicationName() : "문진표 등록 약물";
                
                // 이미 takingPill에 등록되어 있는지 확인 (name과 id가 동일한 값이 있는지)
                boolean alreadyExists = false;
                try {
                    takingPillService.getTakingPillDetailById(user, medicationId);
                    // ID로 조회 성공하면 이미 존재
                    alreadyExists = true;
                    logger.info("Medication with ID {} is already registered in takingPill for user {}", medicationId, user.getId());
                } catch (Exception e) {
                    // ID로 조회 실패하면 존재하지 않음
                    alreadyExists = false;
                }
                
                // name으로도 확인 (동일한 이름의 약물이 있는지)
                if (!alreadyExists) {
                    try {
                        List<com.oauth2.User.TakingPill.Entity.TakingPill> existingPills = takingPillService.getTakingPillsByUser(user);
                        for (com.oauth2.User.TakingPill.Entity.TakingPill pill : existingPills) {
                            // 복호화된 약물명으로 비교
                            String decryptedPillName = getDecryptedMedicationName(pill);
                            if (medicationName.equals(decryptedPillName)) {
                                alreadyExists = true;
                                logger.info("Medication with name '{}' is already registered in takingPill for user {}", medicationName, user.getId());
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Error checking existing medications by name for user {}: {}", user.getId(), e.getMessage());
                    }
                }
                
                if (alreadyExists) {
                    continue; // 이미 등록되어 있으면 건너뛰기
                }
                
                // 등록되어 있지 않으면 새로 등록
                logger.info("Medication {} ({}) is not registered in takingPill, creating new entry for user {}", medicationId, medicationName, user.getId());
                
                // 기본 복용 정보로 TakingPillRequest 생성
                TakingPillRequest takingPillRequest = new TakingPillRequest();
                takingPillRequest.setMedicationId(medicationId);
                takingPillRequest.setMedicationName(medicationName);
                takingPillRequest.setStartDate(LocalDate.now()); // 오늘부터 시작
                takingPillRequest.setEndDate(LocalDate.now().plusYears(1)); // 1년 후까지
                takingPillRequest.setAlarmName("문진표 등록 약물");
                takingPillRequest.setDosageAmount(1.0); // 기본 복용량 1
                takingPillRequest.setDaysOfWeek(List.of("EVERYDAY")); // 매일 복용
                
                // 기본 복용 스케줄 설정 (아침 9시)
                TakingPillRequest.DosageSchedule defaultSchedule = new TakingPillRequest.DosageSchedule();
                defaultSchedule.setHour(9);
                defaultSchedule.setMinute(0);
                defaultSchedule.setPeriod("AM");
                defaultSchedule.setAlarmOnOff(true);
                defaultSchedule.setDosageUnit("정");
                takingPillRequest.setDosageSchedules(List.of(defaultSchedule));
                
                // TakingPill에 등록
                takingPillService.addTakingPill(user, takingPillRequest);
                logger.info("Successfully registered medication {} ({}) to takingPill for user {}", medicationId, medicationName, user.getId());
                
            } catch (NumberFormatException e) {
                logger.warn("Invalid medication ID format: {} for user {}", medication.getMedicationId(), user.getId());
            } catch (Exception e) {
                logger.error("Failed to register medication {} ({}) to takingPill for user {}: {}", 
                    medication.getMedicationId(), medication.getMedicationName(), user.getId(), e.getMessage());
                // 개별 약물 등록 실패가 전체 프로세스를 중단하지 않도록 예외를 다시 던지지 않음
            }
        }
    }

    /**
     * TakingPill의 암호화된 약물명을 복호화합니다.
     */
    private String getDecryptedMedicationName(com.oauth2.User.TakingPill.Entity.TakingPill pill) {
        try {
            String encryptedName = pill.getMedicationName();
            if (encryptedName != null && !encryptedName.isEmpty()) {
                return encryptionUtil.decrypt(encryptedName);
            }
        } catch (Exception e) {
            logger.warn("Failed to decrypt medication name for takingPill {}: {}", pill.getId(), e.getMessage());
        }
        return pill.getMedicationName(); // 복호화 실패 시 원본 반환
    }
} 