// author : mireutale
// description : 사용자 서비스
package com.oauth2.User.UserInfo.Service;

import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.UserInfo.Repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    // 현재 로그인한 사용자 정보 조회
    public User getCurrentUser(Long userId) {
        return userRepository.findByIdWithQuestionnaires(userId)
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
        
        // 닉네임 중복 체크
        if (userRepository.findByNickname(nickname).isPresent()) {
            System.out.println("Nickname already exists: " + nickname);
            throw new RuntimeException("Nickname already exists");
        }
        
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
        boolean isDuplicate = false;
        switch (type.toLowerCase()) {
            case "loginid":
                isDuplicate = userRepository.findByLoginId(value).isPresent();
                if (isDuplicate) {
                    throw new RuntimeException("이미 존재하는 사용자 ID입니다.");
                }
                break;
            case "nickname":
                isDuplicate = userRepository.findByNickname(value).isPresent();
                if (isDuplicate) {
                    throw new RuntimeException("이미 사용 중인 닉네임입니다.");
                }
                break;
            case "phonenumber":
                isDuplicate = userProfileRepository.findByPhone(value).isPresent();
                if (isDuplicate) {
                    throw new RuntimeException("이미 사용 중인 전화번호입니다.");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid check type: " + type);
        }
    }

    // 중복 체크 (boolean 반환)
    public boolean isDuplicate(String value, String type) {
        return switch (type.toLowerCase()) {
            case "loginid" -> userRepository.findByLoginId(value).isPresent();
            case "nickname" -> userRepository.findByNickname(value).isPresent();
            case "phonenumber" -> userProfileRepository.findByPhone(value).isPresent();
            default -> throw new IllegalArgumentException("Invalid check type: " + type);
        };
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
        userRepository.delete(user);
    }


}
