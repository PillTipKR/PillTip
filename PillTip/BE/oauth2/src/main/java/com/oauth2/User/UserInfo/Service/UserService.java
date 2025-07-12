// author : mireutale
// description : 사용자 서비스
package com.oauth2.User.UserInfo.Service;

import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Friend.Repository.FriendRepository;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.UserInfo.Repository.UserProfileRepository;
import com.oauth2.Util.Encryption.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final EncryptionUtil encryptionUtil;
    private final FriendRepository friendRepository;

    // 현재 로그인한 사용자 정보 조회
    public User getCurrentUser(Long userId) {
        return userRepository.findByIdWithQuestionnaire(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
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
            .orElseThrow(() -> new RuntimeException("User profile not found"));
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
        System.out.println("=== UserService.updateNickname ===");
        System.out.println("User ID: " + user.getId());
        System.out.println("Input nickname: '" + nickname + "'");
        System.out.println("Input nickname length: " + nickname.length());
        
        // 닉네임 설정
        user.setNickname(nickname);
        System.out.println("Set nickname to user: '" + user.getNickname() + "'");
        
        // 저장
        User savedUser = userRepository.save(user);
        System.out.println("Saved user nickname: '" + savedUser.getNickname() + "'");
        System.out.println("=== UserService.updateNickname END ===");
        
        return savedUser;
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
                    throw new RuntimeException("이미 존재하는 사용자 ID입니다.");
                case "phonenumber":
                    throw new RuntimeException("이미 사용 중인 전화번호입니다.");
                default:
                    throw new IllegalArgumentException("Invalid check type: " + type);
            }
        }
    }

    // 중복 체크 (boolean 반환)
    public boolean isDuplicate(String value, String type) {
        return switch (type.toLowerCase()) {
            case "loginid" -> checkLoginIdDuplicate(value);
            case "phonenumber" -> checkPhoneNumberDuplicate(value);
            default -> throw new IllegalArgumentException("Invalid check type: " + type);
        };
    }

    // loginId 중복 체크 (암호화된 값과 비교)
    private boolean checkLoginIdDuplicate(String loginId) {
        System.out.println("=== checkLoginIdDuplicate ===");
        System.out.println("Checking for loginId: " + loginId);
        
        List<User> allUsers = userRepository.findAll();
        System.out.println("Total users in database: " + allUsers.size());
        
        for (User user : allUsers) {
            System.out.println("Checking user ID: " + user.getId() + ", LoginId: " + user.getLoginId());
            
            if (user.getLoginId() != null) {
                try {
                    String decryptedLoginId = encryptionUtil.decrypt(user.getLoginId());
                    System.out.println("Decrypted loginId: " + decryptedLoginId);
                    
                    if (decryptedLoginId.equals(loginId)) {
                        System.out.println("Found duplicate loginId!");
                        return true;
                    }
                } catch (Exception e) {
                    System.out.println("Failed to decrypt loginId for user " + user.getId() + ": " + e.getMessage());
                    // 복호화 실패 시 원본 값과도 비교
                    if (user.getLoginId().equals(loginId)) {
                        System.out.println("Found duplicate loginId (original comparison)!");
                        return true;
                    }
                }
            }
        }
        System.out.println("No duplicate loginId found");
        return false;
    }

    // phoneNumber 중복 체크 (암호화된 값과 비교)
    private boolean checkPhoneNumberDuplicate(String phoneNumber) {
        System.out.println("=== checkPhoneNumberDuplicate ===");
        System.out.println("Checking for phoneNumber: " + phoneNumber);
        
        List<UserProfile> allProfiles = userProfileRepository.findAll();
        System.out.println("Total profiles in database: " + allProfiles.size());
        
        for (UserProfile profile : allProfiles) {
            System.out.println("Checking profile for user ID: " + profile.getUserId() + ", Phone: " + profile.getPhone());
            
            if (profile.getPhone() != null) {
                try {
                    String decryptedPhone = encryptionUtil.decrypt(profile.getPhone());
                    System.out.println("Decrypted phone: " + decryptedPhone);
                    
                    if (decryptedPhone.equals(phoneNumber)) {
                        System.out.println("Found duplicate phoneNumber!");
                        return true;
                    }
                } catch (Exception e) {
                    System.out.println("Failed to decrypt phone for user " + profile.getUserId() + ": " + e.getMessage());
                    // 복호화 실패 시 원본 값과도 비교
                    if (profile.getPhone().equals(phoneNumber)) {
                        System.out.println("Found duplicate phoneNumber (original comparison)!");
                        return true;
                    }
                }
            }
        }
        System.out.println("No duplicate phoneNumber found");
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
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 연관된 모든 데이터 삭제
        friendRepository.deleteAllByUserId(user.getId());
        friendRepository.deleteAllByFriendId(user.getId());

        userRepository.delete(user);
    }
}
