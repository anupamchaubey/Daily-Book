package com.DailyBook.controller;

import com.DailyBook.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // ✅ follow a user  (needs JWT)
    @PostMapping("/{username}")
    public Map<String, String> follow(@PathVariable String username, Authentication authentication) {
        String me = authentication.getName();
        followService.follow(me, username);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Followed " + username);
        return res;
    }

    // ✅ unfollow a user (needs JWT)
    @DeleteMapping("/{username}")
    public Map<String, String> unfollow(@PathVariable String username, Authentication authentication) {
        String me = authentication.getName();
        followService.unfollow(me, username);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Unfollowed " + username);
        return res;
    }

    // ✅ PRIVATE: list usernames I follow
    @GetMapping("/me/following")
    public List<String> myFollowing(Authentication authentication) {
        String me = authentication.getName();
        return followService.getFollowingUsernames(me);
    }

    // ✅ PRIVATE: list usernames who follow me
    @GetMapping("/me/followers")
    public List<String> myFollowers(Authentication authentication) {
        String me = authentication.getName();
        return followService.getFollowerUsernames(me);
    }
}
