// com/DailyBook/controller/FollowController.java
package com.DailyBook.controller;

import com.DailyBook.model.Follow;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public String followUser(@PathVariable String userId, Authentication authentication) {
        followService.followUser(authentication.getName(), userId);
        return "Followed successfully";
    }

    @DeleteMapping("/{userId}")
    public String unfollowUser(@PathVariable String userId, Authentication authentication) {
        followService.unfollowUser(authentication.getName(), userId);
        return "Unfollowed successfully";
    }

    @GetMapping("/followers/{userId}")
    public List<Follow> getFollowers(@PathVariable String userId) {
        return followService.getFollowers(userId);
    }

    @GetMapping("/following/{userId}")
    public List<Follow> getFollowing(@PathVariable String userId) {
        return followService.getFollowing(userId);
    }
}
