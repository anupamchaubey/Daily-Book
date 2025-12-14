package com.DailyBook.controller;

import com.DailyBook.dto.UserProfileResponse;
import com.DailyBook.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/users")
@RequiredArgsConstructor
public class PublicUserSearchController {

    private final UserProfileService userProfileService;

    @GetMapping("/search")
    public List<UserProfileResponse> searchUsers(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return List.of();
        }
        return userProfileService.searchUsers(q.trim());
    }
}
