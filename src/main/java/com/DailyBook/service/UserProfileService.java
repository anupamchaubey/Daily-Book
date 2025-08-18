package com.DailyBook.service;

import com.DailyBook.dto.UserProfileRequest;
import com.DailyBook.dto.UserProfileResponse;
import com.DailyBook.model.UserProfile;
import com.DailyBook.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    // ✅ Get logged-in user's profile
    public UserProfileResponse getProfile(String userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return toResponse(profile);
    }

    // ✅ Update logged-in user's profile
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

    // 🔹 Search users by username (partial match)
    public List<UserProfileResponse> searchUsers(String query) {
        return userProfileRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 🔹 Get user profile by username
    public UserProfileResponse getByUsername(String username) {
        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(profile);
    }

    // 🔹 Mapper
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
