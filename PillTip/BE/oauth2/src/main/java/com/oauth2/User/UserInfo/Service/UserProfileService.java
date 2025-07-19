package com.oauth2.User.UserInfo.Service;

import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Entity.UserProfile;
import com.oauth2.User.UserInfo.Repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    public UserProfile getUserProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));
    }

    public UserProfile updatePregnant(User user, boolean pregnant) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfile.setPregnant(pregnant);
        return userProfileRepository.save(userProfile);
    }
}

