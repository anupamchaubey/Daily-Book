package com.DailyBook.controller;

import com.DailyBook.dto.UserProfileRequest;
import com.DailyBook.dto.UserProfileResponse;
import com.DailyBook.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    // ✅ Get logged-in user's profile
    @GetMapping
    public UserProfileResponse getMyProfile(Authentication authentication) {
        return userProfileService.getProfile(authentication.getName());
    }

    // ✅ Update logged-in user's profile
    @PutMapping
    public UserProfileResponse updateMyProfile(
            @RequestBody UserProfileRequest request,
            Authentication authentication) {
        return userProfileService.updateProfile(authentication.getName(), request);
    }

    @GetMapping("/search")
    public List<UserProfileResponse> searchUsers(@RequestParam String q) {
        return userProfileService.searchUsers(q);
    }

    // Get another user's profile by username
    @GetMapping("/{username}")
    public UserProfileResponse getUserProfile(@PathVariable String username) {
        return userProfileService.getByUsername(username);
    }
}
