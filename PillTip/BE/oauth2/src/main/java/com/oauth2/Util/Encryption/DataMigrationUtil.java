// author : mireutale
// description : 기존 데이터 암호화 마이그레이션 유틸리티

package com.oauth2.Util.Encryption;

import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.PatientQuestionnaire.Entity.PatientQuestionnaire;
import com.oauth2.User.PatientQuestionnaire.Repository.PatientQuestionnaireRepository;
import com.oauth2.User.TakingPill.Entity.TakingPill;
import com.oauth2.User.TakingPill.Repositoty.TakingPillRepository;
import com.oauth2.User.UserInfo.Entity.UserSensitiveInfo;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import com.oauth2.User.UserInfo.Repository.UserSensitiveInfoRepository;
import com.oauth2.User.UserInfo.Repository.UserProfileRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataMigrationUtil implements CommandLineRunner {

    @Autowired
    private EncryptionUtil encryptionUtil;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserSensitiveInfoRepository userSensitiveInfoRepository;
    
    @Autowired
    private PatientQuestionnaireRepository patientQuestionnaireRepository;
    
    @Autowired
    private TakingPillRepository takingPillRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;

    @Override
    public void run(String... args) throws Exception {
        // 애플리케이션 시작 시 데이터 마이그레이션 실행
        migrateExistingData();
    }

    @Transactional
    public void migrateExistingData() {
        try {
            System.out.println("=== 기존 데이터 암호화 마이그레이션 시작 ===");
            
            // 1. User 엔티티 마이그레이션
            migrateUserData();
            
            // 2. UserSensitiveInfo 엔티티 마이그레이션
            migrateUserSensitiveInfoData();
            
            // 3. PatientQuestionnaire 엔티티 마이그레이션
            migratePatientQuestionnaireData();
            
            // 4. TakingPill 엔티티 마이그레이션
            migrateTakingPillData();
            
            // 5. UserProfile 엔티티 마이그레이션
            migrateUserProfileData();
            
            System.out.println("=== 기존 데이터 암호화 마이그레이션 완료 ===");
            
        } catch (Exception e) {
            System.err.println("데이터 마이그레이션 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void migrateUserData() {
        System.out.println("User 데이터 마이그레이션 시작...");
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            boolean needsUpdate = false;
            
            // 이메일 암호화
            if (user.getUserEmail() != null && !encryptionUtil.isEncrypted(user.getUserEmail())) {
                try {
                    String encryptedEmail = encryptionUtil.encrypt(user.getUserEmail());
                    user.setUserEmail(encryptedEmail);
                    needsUpdate = true;
                    System.out.println("User ID " + user.getId() + " 이메일 암호화 완료");
                } catch (Exception e) {
                    System.err.println("User ID " + user.getId() + " 이메일 암호화 실패: " + e.getMessage());
                }
            }
            
            // 실명 암호화
            if (user.getRealName() != null && !encryptionUtil.isEncrypted(user.getRealName())) {
                try {
                    String encryptedRealName = encryptionUtil.encrypt(user.getRealName());
                    user.setRealName(encryptedRealName);
                    needsUpdate = true;
                    System.out.println("User ID " + user.getId() + " 실명 암호화 완료");
                } catch (Exception e) {
                    System.err.println("User ID " + user.getId() + " 실명 암호화 실패: " + e.getMessage());
                }
            }
            
            // 주소 암호화
            if (user.getAddress() != null && !encryptionUtil.isEncrypted(user.getAddress())) {
                try {
                    String encryptedAddress = encryptionUtil.encrypt(user.getAddress());
                    user.setAddress(encryptedAddress);
                    needsUpdate = true;
                    System.out.println("User ID " + user.getId() + " 주소 암호화 완료");
                } catch (Exception e) {
                    System.err.println("User ID " + user.getId() + " 주소 암호화 실패: " + e.getMessage());
                }
            }
            
            // 로그인 ID 암호화
            if (user.getLoginId() != null && !encryptionUtil.isEncrypted(user.getLoginId())) {
                try {
                    String encryptedLoginId = encryptionUtil.encrypt(user.getLoginId());
                    user.setLoginId(encryptedLoginId);
                    needsUpdate = true;
                    System.out.println("User ID " + user.getId() + " 로그인 ID 암호화 완료");
                } catch (Exception e) {
                    System.err.println("User ID " + user.getId() + " 로그인 ID 암호화 실패: " + e.getMessage());
                }
            }
            
            // 프로필 사진 URL 암호화
            if (user.getProfilePhoto() != null && !encryptionUtil.isEncrypted(user.getProfilePhoto())) {
                try {
                    String encryptedProfilePhoto = encryptionUtil.encrypt(user.getProfilePhoto());
                    user.setProfilePhoto(encryptedProfilePhoto);
                    needsUpdate = true;
                    System.out.println("User ID " + user.getId() + " 프로필 사진 URL 암호화 완료");
                } catch (Exception e) {
                    System.err.println("User ID " + user.getId() + " 프로필 사진 URL 암호화 실패: " + e.getMessage());
                }
            }

            // 소셜 ID 암호화
            if (user.getSocialId() != null && !encryptionUtil.isEncrypted(user.getSocialId())) {
                try {
                    String encryptedSocialId = encryptionUtil.encrypt(user.getSocialId());
                    user.setSocialId(encryptedSocialId);
                    needsUpdate = true;
                    System.out.println("User ID " + user.getId() + " 소셜 ID 암호화 완료");
                } catch (Exception e) {
                    System.err.println("User ID " + user.getId() + " 소셜 ID 암호화 실패: " + e.getMessage());
                }
            }
            
            if (needsUpdate) {
                userRepository.save(user);
            }
        }
        System.out.println("User 데이터 마이그레이션 완료");
    }

    private void migrateUserSensitiveInfoData() {
        System.out.println("UserSensitiveInfo 데이터 마이그레이션 시작...");
        List<UserSensitiveInfo> sensitiveInfos = userSensitiveInfoRepository.findAll();
        
        for (UserSensitiveInfo info : sensitiveInfos) {
            boolean needsUpdate = false;
            
            // 약물 정보 암호화
            if (info.getMedicationInfo() != null && !encryptionUtil.isEncrypted(info.getMedicationInfo())) {
                try {
                    String encryptedMedication = encryptionUtil.encrypt(info.getMedicationInfo());
                    info.setMedicationInfo(encryptedMedication);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("UserSensitiveInfo ID " + info.getId() + " 약물 정보 암호화 실패: " + e.getMessage());
                }
            }
            
            // 알러지 정보 암호화
            if (info.getAllergyInfo() != null && !encryptionUtil.isEncrypted(info.getAllergyInfo())) {
                try {
                    String encryptedAllergy = encryptionUtil.encrypt(info.getAllergyInfo());
                    info.setAllergyInfo(encryptedAllergy);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("UserSensitiveInfo ID " + info.getId() + " 알러지 정보 암호화 실패: " + e.getMessage());
                }
            }
            
            // 만성질환 정보 암호화
            if (info.getChronicDiseaseInfo() != null && !encryptionUtil.isEncrypted(info.getChronicDiseaseInfo())) {
                try {
                    String encryptedChronic = encryptionUtil.encrypt(info.getChronicDiseaseInfo());
                    info.setChronicDiseaseInfo(encryptedChronic);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("UserSensitiveInfo ID " + info.getId() + " 만성질환 정보 암호화 실패: " + e.getMessage());
                }
            }
            
            // 수술이력 정보 암호화
            if (info.getSurgeryHistoryInfo() != null && !encryptionUtil.isEncrypted(info.getSurgeryHistoryInfo())) {
                try {
                    String encryptedSurgery = encryptionUtil.encrypt(info.getSurgeryHistoryInfo());
                    info.setSurgeryHistoryInfo(encryptedSurgery);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("UserSensitiveInfo ID " + info.getId() + " 수술이력 정보 암호화 실패: " + e.getMessage());
                }
            }
            
            if (needsUpdate) {
                userSensitiveInfoRepository.save(info);
                System.out.println("UserSensitiveInfo ID " + info.getId() + " 암호화 완료");
            }
        }
        System.out.println("UserSensitiveInfo 데이터 마이그레이션 완료");
    }

    private void migratePatientQuestionnaireData() {
        System.out.println("PatientQuestionnaire 데이터 마이그레이션 시작...");
        List<PatientQuestionnaire> questionnaires = patientQuestionnaireRepository.findAll();
        
        for (PatientQuestionnaire questionnaire : questionnaires) {
            boolean needsUpdate = false;
            
            // 약물 정보 암호화
            if (questionnaire.getMedicationInfo() != null && !encryptionUtil.isEncrypted(questionnaire.getMedicationInfo())) {
                try {
                    String encryptedMedication = encryptionUtil.encrypt(questionnaire.getMedicationInfo());
                    questionnaire.setMedicationInfo(encryptedMedication);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("Questionnaire ID " + questionnaire.getQuestionnaireId() + " 약물 정보 암호화 실패: " + e.getMessage());
                }
            }
            
            // 알러지 정보 암호화
            if (questionnaire.getAllergyInfo() != null && !encryptionUtil.isEncrypted(questionnaire.getAllergyInfo())) {
                try {
                    String encryptedAllergy = encryptionUtil.encrypt(questionnaire.getAllergyInfo());
                    questionnaire.setAllergyInfo(encryptedAllergy);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("Questionnaire ID " + questionnaire.getQuestionnaireId() + " 알러지 정보 암호화 실패: " + e.getMessage());
                }
            }
            
            // 만성질환 정보 암호화
            if (questionnaire.getChronicDiseaseInfo() != null && !encryptionUtil.isEncrypted(questionnaire.getChronicDiseaseInfo())) {
                try {
                    String encryptedChronic = encryptionUtil.encrypt(questionnaire.getChronicDiseaseInfo());
                    questionnaire.setChronicDiseaseInfo(encryptedChronic);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("Questionnaire ID " + questionnaire.getQuestionnaireId() + " 만성질환 정보 암호화 실패: " + e.getMessage());
                }
            }
            
            // 수술이력 정보 암호화
            if (questionnaire.getSurgeryHistoryInfo() != null && !encryptionUtil.isEncrypted(questionnaire.getSurgeryHistoryInfo())) {
                try {
                    String encryptedSurgery = encryptionUtil.encrypt(questionnaire.getSurgeryHistoryInfo());
                    questionnaire.setSurgeryHistoryInfo(encryptedSurgery);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("Questionnaire ID " + questionnaire.getQuestionnaireId() + " 수술이력 정보 암호화 실패: " + e.getMessage());
                }
            }
            
            if (needsUpdate) {
                patientQuestionnaireRepository.save(questionnaire);
                System.out.println("Questionnaire ID " + questionnaire.getQuestionnaireId() + " 암호화 완료");
            }
        }
        System.out.println("PatientQuestionnaire 데이터 마이그레이션 완료");
    }

    private void migrateTakingPillData() {
        System.out.println("TakingPill 데이터 마이그레이션 시작...");
        List<TakingPill> takingPills = takingPillRepository.findAll();
        
        for (TakingPill takingPill : takingPills) {
            boolean needsUpdate = false;
            
            // 약물명 암호화
            if (takingPill.getMedicationName() != null && !encryptionUtil.isEncrypted(takingPill.getMedicationName())) {
                try {
                    String encryptedMedicationName = encryptionUtil.encrypt(takingPill.getMedicationName());
                    takingPill.setMedicationName(encryptedMedicationName);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("TakingPill ID " + takingPill.getId() + " 약물명 암호화 실패: " + e.getMessage());
                }
            }
            
            // 알람명 암호화
            if (takingPill.getAlarmName() != null && !encryptionUtil.isEncrypted(takingPill.getAlarmName())) {
                try {
                    String encryptedAlarmName = encryptionUtil.encrypt(takingPill.getAlarmName());
                    takingPill.setAlarmName(encryptedAlarmName);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("TakingPill ID " + takingPill.getId() + " 알람명 암호화 실패: " + e.getMessage());
                }
            }
            
            // 요일 정보 암호화
            if (takingPill.getDaysOfWeek() != null && !encryptionUtil.isEncrypted(takingPill.getDaysOfWeek())) {
                try {
                    String encryptedDaysOfWeek = encryptionUtil.encrypt(takingPill.getDaysOfWeek());
                    takingPill.setDaysOfWeek(encryptedDaysOfWeek);
                    needsUpdate = true;
                } catch (Exception e) {
                    System.err.println("TakingPill ID " + takingPill.getId() + " 요일 정보 암호화 실패: " + e.getMessage());
                }
            }
            
            if (needsUpdate) {
                takingPillRepository.save(takingPill);
                System.out.println("TakingPill ID " + takingPill.getId() + " 암호화 완료");
            }
        }
        System.out.println("TakingPill 데이터 마이그레이션 완료");
    }

    private void migrateUserProfileData() {
        System.out.println("UserProfile 데이터 마이그레이션 시작...");
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        
        for (UserProfile profile : userProfiles) {
            boolean needsUpdate = false;
            
            // 전화번호 암호화
            if (profile.getPhone() != null && !encryptionUtil.isEncrypted(profile.getPhone())) {
                try {
                    String encryptedPhone = encryptionUtil.encrypt(profile.getPhone());
                    profile.setPhone(encryptedPhone);
                    needsUpdate = true;
                    System.out.println("UserProfile ID " + profile.getUserId() + " 전화번호 암호화 완료");
                } catch (Exception e) {
                    System.err.println("UserProfile ID " + profile.getUserId() + " 전화번호 암호화 실패: " + e.getMessage());
                }
            }
            
            if (needsUpdate) {
                userProfileRepository.save(profile);
            }
        }
        System.out.println("UserProfile 데이터 마이그레이션 완료");
    }
} 