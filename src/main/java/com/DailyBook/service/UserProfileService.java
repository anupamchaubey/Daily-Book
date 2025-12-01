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
    public UserProfileResponse getProfile(String userId /* actually username */) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found")); // or custom ProfileNotFoundException
        return toResponse(profile);
    }

    // ✅ Update logged-in user's profile (username is NOT changed here)
    public UserProfileResponse updateProfile(String userId, UserProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElse(UserProfile.builder()
                        .id(userId)
                        .joinedAt(Instant.now())
                        .build());

        // Keep username in sync with authenticated user
        profile.setUsername(userId);
        profile.setBio(request.getBio());
        profile.setProfilePicture(request.getProfilePicture());

        // Ensure joinedAt is not null for older profiles
        if (profile.getJoinedAt() == null) {
            profile.setJoinedAt(Instant.now());
        }

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
                .orElseThrow(() -> new RuntimeException("User not found")); // or custom exception
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
