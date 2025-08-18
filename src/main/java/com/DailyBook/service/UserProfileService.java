package com.DailyBook.service;

import com.DailyBook.dto.UserProfileRequest;
import com.DailyBook.dto.UserProfileResponse;
import com.DailyBook.model.UserProfile;
import com.DailyBook.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileResponse getProfile(String userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return toResponse(profile);
    }

    public UserProfileResponse updateProfile(String userId, UserProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElse(UserProfile.builder()
                        .id(userId)
                        .joinedAt(Instant.now())
                        .build());

        profile.setUsername(request.getUsername());
        profile.setBio(request.getBio());
        profile.setProfilePicture(request.getProfilePicture());

        UserProfile saved = userProfileRepository.save(profile);
        return toResponse(saved);
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .username(profile.getUsername())
                .bio(profile.getBio())
                .profilePicture(profile.getProfilePicture())
                .joinedAt(profile.getJoinedAt())
                .build();
    }
}
