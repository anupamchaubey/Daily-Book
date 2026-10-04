package com.DailyBook.service;

import com.DailyBook.exception.UserNotFoundException;
import com.DailyBook.model.Follow;
import com.DailyBook.repository.FollowRepository;
import com.DailyBook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public void follow(String follower, String followee) {
        if (follower.equals(followee)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        if (!userRepository.existsByUsername(followee)) {
            throw new UserNotFoundException("User not found: " + followee);
        }

        if (followRepository.existsByFollowerUsernameAndFolloweeUsername(follower, followee)) {
            return;
        }

        Follow follow = Follow.builder()
                .followerUsername(follower)
                .followeeUsername(followee)
                .createdAt(Instant.now())
                .build();

        followRepository.save(follow);
    }

    public void unfollow(String follower, String followee) {
        followRepository.deleteByFollowerUsernameAndFolloweeUsername(follower, followee);
    }

    public boolean isFollowing(String follower, String followee) {
        return followRepository.existsByFollowerUsernameAndFolloweeUsername(follower, followee);
    }

    public List<String> getFollowingUsernames(String follower) {
        return followRepository.findByFollowerUsername(follower)
                .stream()
                .map(Follow::getFolloweeUsername)
                .toList();
    }

    public List<String> getFollowerUsernames(String followee) {
        return followRepository.findByFolloweeUsername(followee)
                .stream()
                .map(Follow::getFollowerUsername)
                .toList();
    }
}
