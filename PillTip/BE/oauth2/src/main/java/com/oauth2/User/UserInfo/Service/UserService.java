// author : mireutale
// description : 사용자 서비스
package com.oauth2.User.UserInfo.Service;

import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Friend.Repository.FriendRepository;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.UserInfo.Repository.UserProfileRepository;
import com.oauth2.User.UserInfo.Repository.UserSensitiveInfoRepository;
import com.oauth2.User.UserInfo.Repository.UserPermissionsRepository;
import com.oauth2.User.TakingPill.Repositoty.DosageLogRepository;
import com.oauth2.User.TakingPill.Repositoty.TakingPillRepository;
import com.oauth2.User.TakingPill.Repositoty.DosageScheduleRepository;
import com.oauth2.User.PatientQuestionnaire.Repository.PatientQuestionnaireRepository;
import com.oauth2.User.PatientQuestionnaire.Repository.QuestionnaireQRUrlRepository;
import com.oauth2.User.Alarm.Repository.FCMTokenRepository;
import com.oauth2.User.Auth.Repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.oauth2.User.Auth.Dto.AuthMessageConstants;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FriendRepository friendRepository;
    private final UserSensitiveInfoRepository userSensitiveInfoRepository;
    private final UserPermissionsRepository userPermissionsRepository;
    private final DosageLogRepository dosageLogRepository;
    private final TakingPillRepository takingPillRepository;
    private final DosageScheduleRepository dosageScheduleRepository;
    private final PatientQuestionnaireRepository patientQuestionnaireRepository;
    private final QuestionnaireQRUrlRepository questionnaireQRUrlRepository;
    private final FCMTokenRepository fcmTokenRepository;
    private final UserTokenRepository userTokenRepository;

    // 현재 로그인한 사용자 정보 조회
    public User getCurrentUser(Long userId) {
        return userRepository.findByIdWithQuestionnaire(userId)
                .orElseThrow(() -> new RuntimeException(AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN));
    }

    // 실명과 주소 업데이트
    public User updatePersonalInfo(User user, String realName, String address) {
        user.setRealName(realName);
        user.setAddress(address);
        return userRepository.save(user);
    }

    // 전화번호 업데이트 (UserProfile의 phone 필드 수정)
    public User updatePhoneNumber(User user, String phoneNumber) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException(AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN));
        userProfile.setPhone(phoneNumber);
        userProfileRepository.save(userProfile);
        return user;
    }

    // 이용약관 동의
    public User agreeToTerms(User user) {
        user.setTerms(true);
        return userRepository.save(user);
    }

    // 닉네임 업데이트
    public User updateNickname(User user, String nickname) {
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    // 프로필 사진 업데이트
    public User updateProfilePhoto(User user, String photoUrl) {
        user.setProfilePhoto(photoUrl);
        return userRepository.save(user);
    }

    // 중복 체크 (예외 발생)
    public void checkDuplicate(String value, String type) {
        boolean isDuplicate = isDuplicate(value, type);
        if (isDuplicate) {
            switch (type.toLowerCase()) {
                case "loginid":
                    throw new RuntimeException(AuthMessageConstants.DUPLICATE_LOGIN_ID);
                case "phonenumber":
                    throw new RuntimeException(AuthMessageConstants.DUPLICATE_PHONE_FORMAT);
                default:
                    throw new IllegalArgumentException(AuthMessageConstants.INVALID_CHECK_TYPE + ": " + type);
            }
        }
    }

    // 중복 체크 (boolean 반환)
    public boolean isDuplicate(String value, String type) {
        return switch (type.toLowerCase()) {
            case "loginid" -> checkLoginIdDuplicate(value);
            case "phonenumber" -> checkPhoneNumberDuplicate(value);
            default -> throw new IllegalArgumentException(AuthMessageConstants.INVALID_CHECK_TYPE + ": " + type);
        };
    }

    // loginId 중복 체크 (EncryptionConverter가 자동으로 복호화)
    private boolean checkLoginIdDuplicate(String loginId) {
        List<User> allUsers = userRepository.findAll();
        
        for (User user : allUsers) {
            if (user.getLoginId() != null && user.getLoginId().equals(loginId)) {
                return true;
            }
        }
        return false;
    }

    // phoneNumber 중복 체크 (EncryptionConverter가 자동으로 복호화)
    private boolean checkPhoneNumberDuplicate(String phoneNumber) {
        List<UserProfile> allProfiles = userProfileRepository.findAll();
        
        for (UserProfile profile : allProfiles) {
            if (profile.getPhone() != null && profile.getPhone().equals(phoneNumber)) {
                return true;
            }
        }
        return false;
    }

    // 전화번호로 사용자 조회
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElse(null);
    }

    // 회원 탈퇴
    public void deleteAccount(Long userId) {
        logger.info("회원 탈퇴 시작 - UserId: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN));

        try {
            // 1. FCM 토큰 삭제
            logger.info("FCM 토큰 삭제 시작");
            fcmTokenRepository.deleteByUser(user);
            
            // 2. 사용자 토큰 삭제
            logger.info("사용자 토큰 삭제 시작");
            userTokenRepository.deleteById(userId);
            
            // 3. 복용 로그 삭제 (TakingPill을 참조하므로 먼저 삭제)
            logger.info("복용 로그 삭제 시작");
            dosageLogRepository.deleteAllByUser(user);
            
            // 4. 복용 스케줄 삭제
            logger.info("복용 스케줄 삭제 시작");
            List<com.oauth2.User.TakingPill.Entity.TakingPill> takingPills = takingPillRepository.findByUser(user);
            for (com.oauth2.User.TakingPill.Entity.TakingPill takingPill : takingPills) {
                dosageScheduleRepository.deleteAllByTakingPill(takingPill);
            }
            
            // 5. 복용 중인 약 삭제
            logger.info("복용 중인 약 삭제 시작");
            takingPillRepository.deleteAllByUser(user);
            
            // 6. 문진표 QR URL 삭제
            logger.info("문진표 QR URL 삭제 시작");
            questionnaireQRUrlRepository.deleteByUser(user);
            
            // 7. 문진표 삭제
            logger.info("문진표 삭제 시작");
            patientQuestionnaireRepository.deleteByUser(user);
            
            // 8. 사용자 민감정보 삭제
            logger.info("사용자 민감정보 삭제 시작");
            userSensitiveInfoRepository.deleteByUser(user);
            
            // 9. 사용자 권한 삭제
            logger.info("사용자 권한 삭제 시작");
            userPermissionsRepository.deleteByUser(user);
            
            // 10. 사용자 프로필 삭제
            logger.info("사용자 프로필 삭제 시작");
            userProfileRepository.deleteByUser(user);
            
            // 11. 친구 관계 삭제
            logger.info("친구 관계 삭제 시작");
            friendRepository.deleteAllByUserId(user.getId());
            friendRepository.deleteAllByFriendId(user.getId());
            
            // 12. 최종적으로 사용자 삭제
            logger.info("사용자 삭제 시작");
            userRepository.delete(user);
            
            logger.info("회원 탈퇴 완료 - UserId: {}", userId);
            
        } catch (Exception e) {
            logger.error("회원 탈퇴 실패 - UserId: {}, Error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("회원 탈퇴 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
