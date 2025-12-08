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

    public UserProfileResponse getProfile(String userId /* actually username */) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    // create a default profile for this user
                    UserProfile p = UserProfile.builder()
                            .id(userId)          // you use username as id
                            .username(userId)
                            .bio(null)
                            .profilePicture(null)
                            .joinedAt(Instant.now())
                            .build();
                    return userProfileRepository.save(p);
                });

        return toResponse(profile);
    }



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

    public List<UserProfileResponse> searchUsers(String query) {
        return userProfileRepository.findByUsernameContainingIgnoreCase(query)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserProfileResponse getByUsername(String username) {
        UserProfile profile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found")); // or custom exception
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
