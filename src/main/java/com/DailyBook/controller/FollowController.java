package com.DailyBook.controller;

import com.DailyBook.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {


    private final FollowService followService;

    // SEND REQUEST
    @PostMapping("/{username}")
    public Map<String, String> follow(@PathVariable String username,
                                      Authentication auth) {

        followService.follow(auth.getName(), username);

        return Map.of("message", "Follow request sent");
    }

    // CANCEL FOLLOW / UNFOLLOW
    @DeleteMapping("/{username}")
    public Map<String, String> unfollow(@PathVariable String username,
                                        Authentication auth) {

        followService.unfollow(auth.getName(), username);

        return Map.of("message", "Follow removed");
    }

    // VIEW MY FOLLOWERS (approved only)
    @GetMapping("/me/followers")
    public List<String> followers(Authentication auth) {
        return followService.getFollowerUsernames(auth.getName());
    }

    // VIEW MY FOLLOWING (approved only)
    @GetMapping("/me/following")
    public List<String> following(Authentication auth) {
        return followService.getFollowingUsernames(auth.getName());
    }

    // VIEW PENDING REQUESTS
    @GetMapping("/me/requests")
    public List<String> pending(Authentication auth) {
        return followService.getPendingRequests(auth.getName());
    }

    // APPROVE REQUEST
    @PostMapping("/approve/{username}")
    public Map<String,String> approve(@PathVariable String username,
                                      Authentication auth) {

        followService.approveFollow(auth.getName(), username);

        return Map.of("message","Approved " + username);
    }

    // REJECT REQUEST
    @DeleteMapping("/reject/{username}")
    public Map<String,String> reject(@PathVariable String username,
                                     Authentication auth) {

        followService.rejectFollow(auth.getName(), username);

        return Map.of("message","Rejected " + username);
    }
}
