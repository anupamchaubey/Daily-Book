package com.DailyBook.controller;

import com.DailyBook.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{username}")
    public ResponseEntity<Map<String, String>> follow(@PathVariable String username, Authentication auth) {
        followService.follow(auth.getName(), username);
        return ResponseEntity.ok(Map.of("message", "Successfully followed " + username));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Map<String, String>> unfollow(@PathVariable String username, Authentication auth) {
        followService.unfollow(auth.getName(), username);
        return ResponseEntity.ok(Map.of("message", "Successfully unfollowed " + username));
    }

    @GetMapping("/followers")
    public ResponseEntity<List<String>> getMyFollowers(Authentication auth) {
        return ResponseEntity.ok(followService.getFollowerUsernames(auth.getName()));
    }

    @GetMapping("/following")
    public ResponseEntity<List<String>> getMyFollowing(Authentication auth) {
        return ResponseEntity.ok(followService.getFollowingUsernames(auth.getName()));
    }
}
