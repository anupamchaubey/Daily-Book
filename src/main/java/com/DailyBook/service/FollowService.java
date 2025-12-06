package com.DailyBook.service;

import com.DailyBook.model.Follow;
import com.DailyBook.repository.FollowRepository;
import com.DailyBook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public void follow(String followerUsername, String followeeUsername) {

        if (followerUsername.equals(followeeUsername)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        if (!userRepository.existsByUsername(followeeUsername)) {
            throw new IllegalArgumentException("Target user does not exist");
        }

        if (followRepository.existsByFollowerUsernameAndFolloweeUsername(followerUsername, followeeUsername)) {
            // already following → do nothing
            return;
        }

        Follow follow = Follow.builder()
                .followerUsername(followerUsername)
                .followeeUsername(followeeUsername)
                .createdAt(Instant.now())
                .build();

        followRepository.save(follow);
    }

    public void unfollow(String followerUsername, String followeeUsername) {
        followRepository.deleteByFollowerUsernameAndFolloweeUsername(followerUsername, followeeUsername);
    }

    public boolean isFollowing(String followerUsername, String followeeUsername) {
        return followRepository.existsByFollowerUsernameAndFolloweeUsername(followerUsername, followeeUsername);
    }

    public List<String> getFollowingUsernames(String followerUsername) {
        return followRepository.findByFollowerUsername(followerUsername)
                .stream()
                .map(Follow::getFolloweeUsername)
                .collect(Collectors.toList());
    }

    public List<String> getFollowerUsernames(String followeeUsername) {
        return followRepository.findByFolloweeUsername(followeeUsername)
                .stream()
                .map(Follow::getFollowerUsername)
                .collect(Collectors.toList());
    }
}
