// author : mireutale
// description : 사용자 서비스
package com.oauth2.User.UserInfo.Service;

import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Friend.Repository.FriendRepository;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.UserInfo.Repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.oauth2.User.Auth.Dto.AuthMessageConstants;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FriendRepository friendRepository;

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(AuthMessageConstants.USER_INFO_NOT_FOUND_RETRY_LOGIN));

        // 연관된 모든 데이터 삭제
        friendRepository.deleteAllByUserId(user.getId());
        friendRepository.deleteAllByFriendId(user.getId());

        userRepository.delete(user);
    }
}
