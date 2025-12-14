package com.DailyBook.service;

import com.DailyBook.dto.UserProfileRequest;
import com.DailyBook.dto.UserProfileResponse;
import com.DailyBook.model.UserProfile;
import com.DailyBook.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    // Get or lazily create profile
    public UserProfileResponse getProfile(String userId /* username */) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    UserProfile p = UserProfile.builder()
                            .id(userId)
                            .username(userId)
                            .bio(null)
                            .profilePicture(null)
                            .joinedAt(Instant.now())
                            .build();
                    return userProfileRepository.save(p);
                });

        return toResponse(profile);
    }

    // Update logged-in user's profile
    public UserProfileResponse updateProfile(String userId, UserProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElse(UserProfile.builder()
                        .id(userId)
                        .joinedAt(Instant.now())
                        .build());

        profile.setUsername(userId);
        profile.setBio(request.getBio());
        profile.setProfilePicture(request.getProfilePicture());

        if (profile.getJoinedAt() == null) {
            profile.setJoinedAt(Instant.now());
        }

        UserProfile saved = userProfileRepository.save(profile);
        return toResponse(saved);
    }

    // 🔍 Public people search
    public List<UserProfileResponse> searchUsers(String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        return userProfileRepository
                .findTop15ByUsernameRegex(q.trim())
                .stream()
                .map(this::toResponse)

                .toList();
    }


    // Get profile by username
    public UserProfileResponse getByUsername(String username) {
        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(profile);
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
