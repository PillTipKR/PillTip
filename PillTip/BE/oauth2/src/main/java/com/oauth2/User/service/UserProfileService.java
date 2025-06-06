package com.oauth2.User.service;

import com.oauth2.User.dto.TakingPillRequest;
import com.oauth2.User.entity.User;
import com.oauth2.User.entity.UserProfile;
import com.oauth2.User.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserProfile addTakingPill(User user, TakingPillRequest request) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));

        List<TakingPillRequest> takingPills = getTakingPillsList(userProfile);
        takingPills.add(request);
        return saveTakingPills(userProfile, takingPills);
    }

    public UserProfile deleteTakingPill(User user, String medicationId) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));

        List<TakingPillRequest> takingPills = getTakingPillsList(userProfile);
        takingPills = takingPills.stream()
            .filter(pill -> !pill.getMedicationId().equals(medicationId))
            .collect(Collectors.toList());
        
        return saveTakingPills(userProfile, takingPills);
    }

    public UserProfile updateTakingPill(User user, TakingPillRequest request) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));

        List<TakingPillRequest> takingPills = getTakingPillsList(userProfile);
        takingPills = takingPills.stream()
            .map(pill -> pill.getMedicationId().equals(request.getMedicationId()) ? request : pill)
            .collect(Collectors.toList());
        
        return saveTakingPills(userProfile, takingPills);
    }

    public UserProfile getTakingPill(User user) {
        return userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));
    }

    public UserProfile updatePregnant(User user, boolean pregnant) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        userProfile.setPregnant(pregnant);
        return userProfileRepository.save(userProfile);
    }

    private List<TakingPillRequest> getTakingPillsList(UserProfile userProfile) {
        if (userProfile.getTakingPills() == null || userProfile.getTakingPills().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(userProfile.getTakingPills(), 
                new TypeReference<List<TakingPillRequest>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse taking pills", e);
        }
    }

    private UserProfile saveTakingPills(UserProfile userProfile, List<TakingPillRequest> takingPills) {
        try {
            String takingPillsJson = objectMapper.writeValueAsString(takingPills);
            userProfile.setTakingPills(takingPillsJson);
            return userProfileRepository.save(userProfile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save taking pills", e);
        }
    }
}
